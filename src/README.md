# File Compression System Using Huffman Encoding

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Code and Files](#code-and-files)
- [Getting Started](#getting-started)
- [Prerequisites](#prerequisites)
- [Usage](#usage)
- [Example](#example)


## Overview

This repository contains a Java-based implementation of a file compression system utilizing Huffman encoding. Huffman encoding is a lossless data compression algorithm that is widely used for reducing the size of data files without any loss of information. You can also decompress the file using this app into its original form. This app also has some additional features such as: Given a string of alphabetic characters, you can compress the size of its bit sequence to obtain a binary sequence that has size significantly lesser than the original, this new sequence can then be sent through a network or saved in file so that it can be decompressed whenever required.

## Features

- **Huffman Encoding**: Implements Huffman Tree and Encoding algorithms to compress files efficiently.
- **User Interface**: A simple GUI (`EncoderGUI.java`) to interact with the system.
- **Custom Data Structures**: Includes implementations of various data structures such as singly linked lists, priority queues, and more, tailored for the compression tasks.
- **Progress Tracking**: Visual progress bar to monitor the compression/decompression process.
- **MYSQL database logging**


## Technologies Used:
-Java
-Maven
-JDBC
-MYSQL

## Code and Files
The main code is contained inside [src folder](src)
- `ByteNode.java`: Represents a node containing byte information used in Huffman encoding.
- `CharLinkedList.java`: A linked list implementation for storing characters, used in encoding.
- `CharNode.java`: Represents a node containing character information used in Huffman encoding.
- `Displayer.form` and `Displayer.java`: Handles the display functionalities of the GUI.
- `EncoderGUI.java`: The main GUI for interacting with the compression system.
- `HuffCompression.java`: Core logic for compressing and decompressing files using Huffman encoding.
- `HuffmanEncoder.java`: Contains methods to generate Huffman codes and encode files.
- `HuffmanTree.java`: Constructs the Huffman tree necessary for encoding.
- `Message.java`: Handles messaging within the application.
- `MinPriorityQueue.java`: Implementation of a priority queue, essential for Huffman tree construction.
- `Node.java`: Basic node structure used in various data structures.
- `ProgressBar.java`: Implements the progress bar feature in the GUI.
- `QueueLL.java`: A queue implementation using linked lists.
- `SinglyLinkedList.java`: Basic singly linked list implementation.
- `ThreadedText.java`: Manages text in a multithreaded environment.
- `LoginFrame.java` : Implemented Using MYSQL to connect to database to check for User and Pass using JDBC driver.


## Getting Started

### Prerequisites

- Java Development Kit (JDK) 8 or higher.
- A Java IDE (e.g., IntelliJ IDEA, Eclipse) or any text editor with Java support.

### Usage

1. **Compile the project**: Ensure all `.java` files are compiled. Most IDEs handle this automatically.
2. **Run the GUI**:
   - Execute `EncoderGUI.java` to launch the user interface.
   - Use the provided options to compress or decompress files.
3. **Command Line Interface (CLI)**:
   - If you prefer a non-GUI approach, modify the `HuffCompression.java` to take input from the command line.

### Example

- Compress a text file:
  1. Open the GUI.
  2. Select the file you want to compress.
  3. Click on the "Compress" button and specify the destination for the compressed file.

- Decompress a file:
  1. Open the GUI.
  2. Select the compressed file.
  3. Click on the "Decompress" button and choose where to save the decompressed file.


I added a simple login screen in Java Swing that talks to a MySQL database using the JDBC driver. I’m running MySQL locally and created the compressiondb database and users table from the MySQL command line, then connected to it from Java with a JDBC URL like jdbc:mysql://localhost:3306/compressiondb. When you type your username and password in the login window, the app sends a query to MySQL over JDBC to check if that user exists, and only then opens the main Huffman Encoder UI.