# Key(store) generate

## Create new private key (if you have it already, go to next step)

`openssl genrsa -out private-key.pem 2048`

## Create configuration for certificate

## Create certificate

`openssl req -new -x509 -days 365 -key private-key.pem -config configuration.cnf -out certificate.pem`

## Create keystore

### PKCS#12 file using OpenSSL
`openssl pkcs12 -export -in certificate.pem -inkey private-key.pem -out keystore.p12`

You should be able to use the resulting file directly using the `PKCS12` keystore type. If you really need to, you can convert it to JKS using `keytool -importkeystore` (available in `keytool` from Java 6):

### JKS

`keytool -importkeystore -srckeystore keystore.p12 -srcstoretype PKCS12 -destkeystore keystore.jks -deststoretype JKS`

Use `JKS` keystore for Smart House key AND trust store with password provided above.

## External links
 - http://stackoverflow.com/questions/6252045/creating-a-keystore-from-private-key-and-a-public-key

# Export private key from keystore

Export from `keytool`'s proprietary format (called `JKS`) to standardized format `PKCS #12`:

`keytool -importkeystore -srckeystore keystore.jks -destkeystore keystore.p12 -deststoretype PKCS12 -srcalias <jkskeyalias> -deststorepass <password> -destkeypass <password>`

## Export certificate using `openssl`

`openssl pkcs12 -in keystore.p12  -nokeys -out cert.pem`

## Export unencrypted private key

`openssl pkcs12 -in keystore.p12  -nodes -nocerts -out key.pem`

## External links
 - http://security.stackexchange.com/questions/3779/how-can-i-export-my-private-key-from-a-java-keytool-keystore
