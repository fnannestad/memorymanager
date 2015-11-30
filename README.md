# memorymanager
A memory manager that handles reads and writes from multiple client threads.

I wrote this as an assignment for a third year subject called *Operating Systems and Computer Architecture*.

### Overview
In the **lru_replacement** program the server processes a list of requests and sends them to the client to log. Initially the server reads the number of pages specified by the command line into a buffer (from *sample_files/init_buffer_pages.dat*), then all pages are copied to a random access store file (*output/store_file.dat*). Pages are assumed to have a fixed size of 4096 characters.

If a request (from file *sample_files/all_requests.dat*) is read for a non-buffer resident page, one page that is in the buffer is evicted for the requested page to be loaded from the store file. The page replacement policy used is the Least Recently Used policy.

When a page is evicted it is flushed to the store file if it has been modified since its last load from disk, otherwise it is discarded.

Each client outputs a log of pages it has read in a file called *output/client_log_n*, where *n* is its thread id. These are written in the order they are received from the server.

The server keeps a log of the sequence of page evictions called *output/server_log.dat*. Entries are in the order they were evicted.

The **client_requests** program does not use a page replacement algorithm, but instead alters the program so that requests are issued from clients rather than from the server.

Each client reads its requests from an input file called *sample_files/client_requests_n.dat*, where *n* is the client's thread id.

The server initialises its memory in the same way as the lru_replacement program, but this time each client thread reads from its request file and sends requests to the server in the order they are read. The server processes the requests in ascending order of request number.

Client log files are kept the same way as in lru_replacement.

### Usage
There are no extenal dependancies. Simply compile the files and run with the appropriate command line arguments.

For **lru_replacement** use
```
java Server num_clients buffer_size
```

Where *num_clients* is the number of client threads to be created, and *buffer_size* is the number of pages to be stored in the memory buffer.

For **client_requests** use
```
java Server num_clients
```

Where *num_clients* is the number of client threads to be created.
