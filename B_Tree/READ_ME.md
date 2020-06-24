# B_Tree

Upgraded from Web_Page_Categorization, this project is new and improved! The program expects 10 URL provided in a text document, which is read, scraped and then web crawled until 100 this has been performed with 100 web pages. IO is reduced with a buffered cache and runtime is improved with pre-categorized clusters. Please note, this project loads 100 URLs and saves persistently on the machine!  

Key implementations upgraded in this project:

* Persistent file-based B-Tree
* Fixed size buffer cache
* 5 cluster categorization with kmeans metric
