
# Pulsar Authentication

<!-- TOC depthFrom:2 depthTo:4 withLinks:1 updateOnSave:1 orderedList:0 -->

- [Authentication model](#authentication-model)
- [Authentication providers](#authentication-providers)
	- [TLS client auth](#tls-client-auth)
		- [Creating the certificates](#creating-the-certificates)
		- [Configure broker](#configure-broker)
		- [Configure discovery service](#configure-discovery-service)
		- [Configure Java client](#configure-java-client)
		- [Configure CLI tools](#configure-cli-tools)

<!-- /TOC -->

## Authentication model

Pulsar supports a pluggable authentication mechanism and a broker can be
configured to support multiple authentication sources.

The role of the Authentication provider implementation is to establish the
identity of the client, in the form of a *role* token. This role token
is then used to verify whether this client is allowed to publish or
consume on a certain topic.

## Authentication providers

### TLS client auth

In addition to provide connection encryption between pulsar clients and
broker, TLS can be used to identify the client through a certificate
signed by a trusted certificate authority.

**Note**: Unlike the rest of the Pulsar code, the TLS auth provider is not being
used in production at Yahoo. Please report any issues encountered in using it.

#### Creating the certificates

##### Certificate authority

The first step is to create the certificate for the CA. The CA will be
used to signed both the broker and clients certificates, to ensure each
party will trust the others.

```shell
# On Linux systems:
$ CA.pl -newca

# On MacOSX
$ /System/Library/OpenSSL/misc/CA.pl -newca
```

After answering the questions, this will leave the CA related files
under `./demoCA` directory
 * `demoCA/cacert.pem` is the public certificate. It is meant to be
   distributed to all the parties involved.
 * `demoCA/private/cakey.pem` is the private key. This is only needed
   when signing a new certificate for either broker or clients and it
   must be guarded safely.

##### Broker certificate

Now we can create certificate requests and sign them with the CA.

These commands will ask a few questions and create the certificates. When asked
for the common name, you need to match the hostname of the broker. You could also
use a wildcard to match a group of broker hostnames, eg: `*.broker.usw.example.com`,
so that the same certificate can be reused in multiple machines.

```shell
$ openssl req -newkey rsa:2048 -sha256 -nodes -out broker-cert.csr -outform PEM

# Convert key to PKCS#8 format
$ openssl pkcs8 -topk8 -inform PEM -outform PEM -in privkey.pem -out broker-key.pem -nocrypt
```

This will create a broker certificate request files named
 `broker-cert.csr` and `broker-key.pem`.


We can now proceed to create the signed certificate:

```shell
$ openssl ca -out broker-cert.pem -infiles broker-cert.csr
```

At this point, we have the `broker-cert.pem` and `broker-key.pem` that
are needed for the broker.

##### Client certificate

Repeat the same steps did for the broker and create a `client-cert.pem`
and `client-key.pem` files.

For client common name you need to use a string you intend to use as the
*role* token for this client, though it doesn't need to match the client hostname.

#### Configure broker

To configure TLS authentication in Pulsar broker in `conf/broker.conf`:

```shell
tlsEnabled=true
tlsCertificateFilePath=/path/to/broker-cert.pem
tlsKeyFilePath=/path/to/broker-key.pem
tlsTrustCertsFilePath=/path/to/cacert.pem

# Add TLS auth provider
authenticationEnabled=true
authorizationEnabled=true
authenticationProviders=com.yahoo.pulsar.broker.authentication.AuthenticationProviderTls
```

#### Configure discovery service

Since discovery service is redirecting the HTTPS requests, it needs to be trusted
by the client as well. Add TLS configuration in `conf/discovery.conf`:

```shell
tlsEnabled=true
tlsCertificateFilePath=/path/to/broker-cert.pem
tlsKeyFilePath=/path/to/broker-key.pem
```

#### Configure Java client

```java
ClientConfiguration conf = new ClientConfiguration();
conf.setUseTls(true);
conf.setTlsTrustCertsFilePath("/path/to/cacert.pem");

Map<String, String> authParams = new HashMap<>();
authParams.put("tlsCertFile", "/path/to/client-cert.pem");
authParams.put("tlsKeyFile", "/path/to/client-cert.pem");
conf.setAuthentication(AuthenticationTls.class.getName(), authParams);

PulsarClient client = PulsarClient.create(
                        "https://my-broker.com:4443", conf);
```

#### Configure CLI tools

Command line tools like `pulsar-admin`, `pulsar-perf` and `pulsar-client` use the `conf/client.conf` config file and we can
add there the authentication parameters:

```shell
serviceUrl=https://broker.example.com:8443/
authPlugin=com.yahoo.pulsar.client.impl.auth.AuthenticationTls
authParams=tlsCertFile:/path/to/client-cert.pem,tlsKeyFile:/path/to/client-cert.pem
useTls=true
tlsAllowInsecureConnection=false
tlsTrustCertsFilePath=/path/to/cacert.pem
```
