#generate a keystore
keytool -genkey -alias ironrhino -keyalg RSA -keystore keystore
enter password:ironrhino
#note CN=your domain,eg:localhost,ironrhino.com

#export crt
keytool -export -file server.crt -alias ironrhino -keystore keystore
enter password:ironrhino

#import crt in cas client machine
keytool -import -keystore "%JAVA_HOME%"\jre\lib\security\cacerts -file server.crt -alias ironrhino
enter password:changeit


#pem format
keytool -export -rfc -keystore keystore -alias ironrhino> cert.pem

