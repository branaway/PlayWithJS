jsPlay - A play/Nashorn controller adapter

The core code:

1. conf/route. The mapping of /js/_module/_method to JSController.process
2. The JSController.process method, which marshals the invocation of the JavaScripts. 
3. The js directory where all JavaScipt file must be located. 
4. the js/etc directory that contains a playHeaders.js and a generated js file named modelHeasers.js. 
The modelHeaders.js is automatically updated when model classes are redefined. 
5. conf/data.yml, the initial data objects for the in memory JDBC database

The js/books.js demos all the variations of JPA API to access sql database:

