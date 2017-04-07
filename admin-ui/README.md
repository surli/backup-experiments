# AeroGear Unified Push Server - Admin UI [![Build Status](https://travis-ci.org/aerogear/aerogear-unifiedpush-server-admin-ui.png)](https://travis-ci.org/aerogear/aerogear-unifiedpush-server-admin-ui)

### Setting Up The Development Environment

First make sure you have installed in you machine:

* Node.js - [Download Here](http://nodejs.org/)
* Bower( version >= 0.9.1 ) - [Download Here](https://bower.io/#install-bower)
* Grunt - [Download Here](https://gruntjs.com/getting-started)

First you should have built the whole project with `mvn`

```
mvn clean install
```

Then install all project dependencies by running:
    
    npm install && bower install

UPS is run using a {Wildfly-10|EAP 7} server so one of these servers will need to be installed.

To start the server run the command:

```
$SERVER_HOME/bin/standalone.sh -c standalone-full.xml -b 0.0.0.0
```

and to configure the server for UPS run the command:

```
$SERVER_HOME/bin/jboss-cli.sh --file=/path/to/aerogear-unifiedpush-server/configuration/jms-setup-wildfly.cli
```

The Database and Authentication need to be set now.

Copy `databases/unifiedpush-h2-ds.xml` into the `standalone/deployments` directory. This will be the database.
```
cd $SERVER_HOME/standalone/deployments
cp path/to/aerogear-unifiedpush-server/databases/unifiedpush-h2-ds.xml unifiedpush-h2-ds.xml
```
And copy auth-server.war to `standalone/deployments` for authentication.
```
cp -r path/to/aerogear-unifiedpush-server/servers/auth-server/target/auth-server.war auth-server.war
```

Now lets add `ag-push` to the server. After building the whole project with `mvn`, you will have these two files located in `aerogear-unifiedpush-server/servers/ups-wildfly/target/`:

```
ag-push.war
ag-push
```
`ag-push.war` can be deployed directly but in order for Grunt to work properly, `ag-push` must be deployed instead. To do that, simply rename it to `ag-push.war` and move it to the `standalone/deployments` directory:

```
cp -r path/to/aerogear-unifiedpush-server/servers/ups-wildfly/target/ag-push ag-push.war
```

Now you need to to indicate that `ag-push` should be deployed. Within the `standalone/deployments` directory run:
```
touch ag-push.war.dodeploy
```
Now Grunt must be configured:

    grunt initLocalConfig

This is more likely to fail first time. It will create a file named `local-config.json` that you have to edit to suit your local environment:

    {
        "home": "/path/to/aerogear-unified-push-server/admin-ui",
        "jbossweb": "/path/to/server_home/standalone/deployments/ag-push.war",
    }


 After this there should be no more errors.

> NOTE: This is now required for all grunt steps below because of bug in assemble-less - once fixed, it will be required just for development_

Now the UI should be accessible at http://localhost:8080/ag-push. NOTE: the default user/password is admin:123

Finally, to start developing, run:

    grunt server

Now anytime you save a file, grunt will deploy the UI and you will see the changes after manually refreshing the browser. No further steps are necessary.


### Generate distribution

To create a distribution in `admin-ui/dist/` directory:

    grunt dist
    
To create a distribution and copy it jbossweb folder (as configured above):

    grunt jbosswebDist
    
    
In order to create a WAR application, go into `../server/` folder and run Maven build:

    cd ../server/
    mvn clean install
    

### Cleaning the Admin UI build

For sake of quick development turnaround, the `$ mvn clean` will clean just `dist/` and `.tmp/` build directories, but some frontend build related directories will be still cached (`node/`, `node_modules/`, `app/bower_components/`, `.build-tmp`). In order to clean all build related caches, execute:

    mvn clean install -Dfrontend.clean.force


### Managing NPM packages

The versions of packages listed in `package.json` and their transitive dependencies has to be locked down leveraging [NPM Shrinkwrap tool](http://blog.nodejs.org/2012/02/27/managing-node-js-dependencies-with-shrinkwrap/) (standard part of NPM distribution).

Use of [semantic versioning](https://github.com/npm/node-semver) in NPM makes Node module versions resolution in `package.json` undeterministic. `npm-shrinkwrapp.json` is an equivalent of `package.json` that locks down all the transitive dependencies.

#### Use of shrink-wrapped NPM configuration

For final user, nothing changes:

    npm install

You just need to be aware that `npm-shrinkwrap.json` configuration takes precedence.

#### Upgrading dependencies

The biggest change comes with changing dependency versions, since simple change of `package.json` won't have any effect. In order to upgrade a package, you can use approach like following one:

    $ npm install <package>@<version> --save--dev

Test the build to verify that the new versions work as expected

To lock down version again:

    $ npm shrinkwrap --dev
    $ git add package.json npm-shrinkwrap.json
    $ git commit -m "upgrading <package> to <version>"

Alternatively, you can remove `npm-shrinkwrap.json` and generate a new one.

### Build errors

The `frontend-maven-plugin` build may suffer from inconsistent downloads when you killed the previous build prematurely. This typically leads to such errors:

    [INFO] --- frontend-maven-plugin:0.0.16:grunt (grunt build) @ unifiedpush-admin-ui ---
    [INFO] Running 'grunt dist --no-color'
    [INFO] module.js:340
    [INFO]     throw err;
    [INFO]           ^
    [INFO] Error: Cannot find module 'findup-sync'

or

    [INFO] --- frontend-maven-plugin:0.0.16:npm (npm install) @ unifiedpush-admin-ui ---
    [INFO] Running 'npm install --color=false'
    [INFO] npm ERR! cb() never called!
    [INFO] npm ERR! not ok code 0

The build currently can't recover itself from these error.

In order to fix this issue, you should fully clean the `admin-ui/` build resources:

    mvn clean install -Dfrontend.clean.force
