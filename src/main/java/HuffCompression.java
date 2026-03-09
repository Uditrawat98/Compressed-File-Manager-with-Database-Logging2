import java.util.*;
import java.io.*;

public class HuffCompression {
    private static final StringBuilder sb = new StringBuilder();
    private static final Map<Byte, String> huffmap = new HashMap<>();

    private static final class ZipResult {
        final byte[] bytes;
        final int bitLength;

        private ZipResult(byte[] bytes, int bitLength) {
            this.bytes = bytes;
            this.bitLength = bitLength;
        }
    }

    public static void compress(String src, String dst) {
        try {
            byte[] b = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(src));
            ZipResult zipped = createZip(b);

            try (OutputStream outStream = new FileOutputStream(dst);
                 ObjectOutputStream objectOutStream = new ObjectOutputStream(outStream)) {
                objectOutStream.writeObject(zipped.bytes);
                objectOutStream.writeObject(huffmap);
                objectOutStream.writeInt(zipped.bitLength);
                objectOutStream.writeUTF(getExtensionWithDot(src));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static ZipResult createZip(byte[] bytes) {
        sb.setLength(0);
        huffmap.clear();
        MinPriorityQueue<ByteNode> nodes = getByteNodes(bytes);
        ByteNode root = createHuffmanTree(nodes);
        Map<Byte, String> huffmanCodes = getHuffCodes(root);
        return zipBytesWithCodes(bytes, huffmanCodes);
    }

    private static MinPriorityQueue<ByteNode> getByteNodes(byte[] bytes) {
        MinPriorityQueue<ByteNode> nodes = new MinPriorityQueue<ByteNode>();
        Map<Byte, Integer> tempMap = new HashMap<>();
        for (byte b : bytes) {
            Integer value = tempMap.get(b);
            if (value == null)
                tempMap.put(b, 1);
            else
                tempMap.put(b, value + 1);
        }
        for (Map.Entry<Byte, Integer> entry : tempMap.entrySet())
            nodes.add(new ByteNode(entry.getKey(), entry.getValue()));
        return nodes;
    }

    private static ByteNode createHuffmanTree(MinPriorityQueue<ByteNode> nodes) {
        while (nodes.len() > 1) {
            ByteNode left = nodes.poll();
            ByteNode right = nodes.poll();
            ByteNode parent = new ByteNode(null, left.frequency + right.frequency);
            parent.left = left;
            parent.right = right;
            nodes.add(parent);
        }
        return nodes.poll();
    }

    private static Map<Byte, String> getHuffCodes(ByteNode root) {
        if (root == null) return null;
        getHuffCodes(root.left, "0", sb);
        getHuffCodes(root.right, "1", sb);
        return huffmap;
    }

    private static void getHuffCodes(ByteNode node, String code, StringBuilder sb1) {
        StringBuilder sb2 = new StringBuilder(sb1);
        sb2.append(code);
        if (node != null) {
            if (node.data == null) {
                getHuffCodes(node.left, "0", sb2);
                getHuffCodes(node.right, "1", sb2);
            } else
                huffmap.put(node.data, sb2.toString());
        }
    }

    private static ZipResult zipBytesWithCodes(byte[] bytes, Map<Byte, String> huffCodes) {
        StringBuilder bitString = new StringBuilder();
        for (byte b : bytes) {
            bitString.append(huffCodes.get(b));
        }

        int bitLength = bitString.length();
        int length = (bitLength + 7) / 8;
        byte[] huffCodeBytes = new byte[length];

        for (int idx = 0; idx < length; idx++) {
            int start = idx * 8;
            int end = Math.min(start + 8, bitLength);
            String chunk = bitString.substring(start, end);
            if (chunk.length() < 8) {
                StringBuilder padded = new StringBuilder(chunk);
                while (padded.length() < 8) padded.append('0');
                chunk = padded.toString();
            }
            huffCodeBytes[idx] = (byte) Integer.parseInt(chunk, 2);
        }
        return new ZipResult(huffCodeBytes, bitLength);
    }

    public static String decompress(String src, String dst) throws IOException, ClassNotFoundException {
        try (FileInputStream inStream = new FileInputStream(src);
             ObjectInputStream objectInStream = new ObjectInputStream(inStream);
             ) {

            byte[] huffmanBytes = (byte[]) objectInStream.readObject();
            Map<Byte, String> huffmanCodes =
                    (Map<Byte, String>) objectInStream.readObject();

            int bitLength = -1;
            String originalExt = "";
            try {
                bitLength = objectInStream.readInt();
            } catch (EOFException ignored) {
                // Older compressed files won't have a stored bit length.
            }
            try {
                originalExt = objectInStream.readUTF();
            } catch (EOFException ignored) {
                // Older compressed files won't have the original extension.
            }

            byte[] bytes = decomp(huffmanCodes, huffmanBytes, bitLength);

            String outPath = resolveOutputPath(dst, originalExt);
            try (OutputStream outStream = new FileOutputStream(outPath)) {
                outStream.write(bytes);
            }
            return outPath;
        }
    }

    public static byte[] decomp(Map<Byte, String> huffmanCodes, byte[] huffmanBytes, int bitLength) {
        StringBuilder sb1 = new StringBuilder(huffmanBytes.length * 8);
        if (bitLength > 0) {
            for (byte b : huffmanBytes) {
                int v = b & 0xFF;
                String s = Integer.toBinaryString(v);
                // pad to 8 bits
                for (int i = s.length(); i < 8; i++) sb1.append('0');
                sb1.append(s);
            }
            if (bitLength < sb1.length()) sb1.setLength(bitLength);
        } else {
            // Backward compatible path (may fail for some binary files)
            for (int i = 0; i < huffmanBytes.length; i++) {
                byte b = huffmanBytes[i];
                boolean flag = (i == huffmanBytes.length - 1);
                sb1.append(convertbyteInBit(!flag, b));
            }
        }

        Map<String, Byte> map = new HashMap<>();
        for (Map.Entry<Byte, String> entry : huffmanCodes.entrySet()) {
            map.put(entry.getValue(), entry.getKey());
        }
        java.util.List<Byte> list = new java.util.ArrayList<>();
        for (int i = 0; i < sb1.length();) {
            int count = 1;
            boolean flag = true;
            Byte b = null;
            while (flag) {
                String key = sb1.substring(i, i + count);
                b = map.get(key);
                if (b == null) count++;
                else flag = false;
            }
            list.add(b);
            i += count;
        }
        byte b[] = new byte[list.size()];
        for (int i = 0; i < b.length; i++)
            b[i] = list.get(i);
        return b;
    }

    private static String convertbyteInBit(boolean flag, byte b) {
        int byte0 = b;
        if (flag) byte0 |= 256;
        String str0 = Integer.toBinaryString(byte0);
        if (flag || byte0 < 0)
            return str0.substring(str0.length() - 8);
        else return str0;
    }

    private static String getExtensionWithDot(String path) {
        if (path == null) return "";
        int dot = path.lastIndexOf('.');
        if (dot != -1 && dot < path.length() - 1) {
            return path.substring(dot);
        }
        return "";
    }

    private static String resolveOutputPath(String dst, String originalExtWithDot) {
        if (dst == null || dst.isEmpty()) return dst;
        if (originalExtWithDot == null) originalExtWithDot = "";
        if (originalExtWithDot.isEmpty()) return dst;

        String lowerDst = dst.toLowerCase(Locale.ROOT);
        String lowerExt = originalExtWithDot.toLowerCase(Locale.ROOT);
        if (lowerDst.endsWith(lowerExt)) return dst;

        int lastSlash = Math.max(dst.lastIndexOf('/'), dst.lastIndexOf('\\'));
        int lastDot = dst.lastIndexOf('.');
        boolean hasExt = lastDot > lastSlash;
        if (!hasExt) {
            return dst + originalExtWithDot;
        }
        return dst;
    }
}