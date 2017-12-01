This is a command-line tool to test Twitter REST API with your application credentials.

General syntax is: java -jar twac.jar \<-arh\> \<parameters...\>
-h, --help for displaying short help
-a, --authorize to get PIN-based authentication for the program
-r, --request to execute actual API request

Call format with parameters: 
java -jar twac.jar \<-a, --authorize\> \<consumer key\> \<consumer secret key\>
java -jar twac.jar \<-r, --request\> \<output file\> \<HTTP method\> \<HTTP endpoint with call parameters\>

The program will output formatted JSON file at your \<output file\> path overwriting it if it exists.
Warning: -a stores the API keys by overwriting twac_data.aut without prompt in the same directory where twac.jar is located.
