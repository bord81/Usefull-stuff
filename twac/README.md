This is a command-line tool to test Twitter REST API with your application credentials.

You can take the jar with the lib directory or compile it yourself from source.

Syntax is: java -jar twac.jar \<Output file\> \<HTTP method\> \<HTTP endpoint\> \<Consumer key\> \<Consumer secret key\> \<OAuth access token\> \<OAuth token secret\>

\<Output file\> - path to your local file;
\<HTTP method\> - usually POST or GET;
\<HTTP endpoint\> - Twitter resource URL;
\<Consumer key\> and <Consumer secret key\> - your app credentials;
\<OAuth access token\> and \<OAuth token secret\> - test tokens for your app.

To get this info run: java -jar twac.jar -help

The program will output formatted JSON file at your \<Output file\> path overwriting if it exists and optionally saving unformatted response to \<Output file\> + 'unf' path (if it does not exist).
