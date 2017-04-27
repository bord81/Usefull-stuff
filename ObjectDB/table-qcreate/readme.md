This code is intended to speed up the filling of ObjectDB tables popular for test/embedded configurations.
It works by first serializing your entity class to the file and then it fills the selected ObjectDB file with the information
from stdin.

Comments in the source code an below should help you to get through the process.

How to use it:

1) Create two classes in your existing project or create a new one. Copy and paste the code from here.
2) Add objectdb.jar to your dependencies.
3) Create an entity class representing your table in ObjectDB. It should implement Serializable.
4) Modify two(2) lines in Serializer class - file name to save the entity class and its instantiation line.
5) Run main method in ODBCreate class and select 's'.
6) If all is OK, provide the database file name in ODBCreate class and run the main method with 'e' option.
7) Enter the number of entries and provide values for them.

Optionally you can add/change data types depending on your table (see comments in ODBCreate class), but for most common
cases with int/long and String fields the current version should be OK.
