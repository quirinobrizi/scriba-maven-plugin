# Scriba maven plugin
Scriba maven plugin can be integrated into your build adding one of the following to the plugin section of your Apache Maven pom.xml:

#### To send the generated document to a remote service 

```xml
<properties>
	<scriba.username>my-username</scriba.username>
	<scriba.password>my-password</scriba.password>
	<scriba.authenticate.url>https://authentication.service.org/autenticate</scriba.authenticate.url>
    <scriba.target.uri>https://document.service.org/documents</scriba.target.uri>
    <scriba.maven.plugin.version>0.0.1</scriba.maven.plugin.version>
</properties>

<build>
	<plugins>
		<plugin>
			<groupId>codesketch.scriba</groupId>
			<artifactId>scriba-maven-plugin</artifactId>
			<version>${scriba.maven.plugin.version}</version>
			<executions>
				<execution>
					<phase>compile</phase>
					<goals>
						<goal>document</goal>
					</goals>
				</execution>
			</executions>
			<configuration>
				<interfaces>
					<interface>[your API interface (i.e. codesketch.scriba.test.BookInterface)]</interface>
				</interfaces>
				<environments>
					<environment>
						<name>example</name>
						<endpoint>http://example.endpoint.org</endpoint>
					</environment>
				</environments>
				<credential>
					<username>${scriba.username}</username>
					<password>${scriba.password}</password>
				</credential>
				<authenticateUrl>${scriba.authenticate.url}</authenticateUrl>
				<targetUrl>${scriba.target.uri}</targetUrl>
			</configuration>
		</plugin>
	</plugins>
</build>
```

The above will generate a document similar the the one described on the above example and send it the the target URL as a POST. Before the document is sent the plugin will try authenticate with the remote service using the provided credential and authenticate endpoint.

The authenticate request will be sent as a post and the payload will be:

```json
{
	"email": "<your defined email>"
	"password": "<your defined password>"
}
```

The plugin will accept any response but will inspect it to retrieve the "accessToken" field value, that will be set on the document upload request as a Bearer on the standard HTTP Authorization header.

#### To write the generated document on the file system 

```xml
<properties>
    <scriba.target.uri>file:///path/to/scriba.document.json</scriba.target.uri>
    <scriba.maven.plugin.version>0.0.1</scriba.maven.plugin.version>
</properties>

<build>
	<plugins>
		<plugin>
			<groupId>codesketch.scriba</groupId>
			<artifactId>scriba-maven-plugin</artifactId>
			<version>${scriba.maven.plugin.version}</version>
			<executions>
				<execution>
					<phase>compile</phase>
					<goals>
						<goal>document</goal>
					</goals>
				</execution>
			</executions>
			<configuration>
				<interfaces>
					<interface>[your API interface (i.e. codesketch.scriba.test.BookInterface)]</interface>
				</interfaces>
				<environments>
					<environment>
						<name>example</name>
						<endpoint>http://example.endpoint.org</endpoint>
					</environment>
				</environments>
				<targetUrl>${scriba.target.uri}</targetUrl>
			</configuration>
		</plugin>
	</plugins>
</build>
```

The above will generate a document at the specified ${scriba.target.uri} path.