# Development
## Prerequisites

You should make sure that the following components are installed on your machine:
 - [Node.js](https://nodejs.org/en/download/)
 - [Yarn](https://yarnpkg.com/en/docs/install)
 - JDK 11+
 - [sbt](https://www.scala-sbt.org/download.html)

For azure functions development:
 - [localtunnel](https://theboroer.github.io/localtunnel-www/)
 - [Azure Functions Core Tools](https://learn.microsoft.com/en-us/azure/azure-functions/functions-run-local)

## Setup

- Run `npm install` in the `./app/` directory
- Run `lt --port 7071 --print-requests`. The output of this command will be used in setup and running as `tunnel_link`.
- Create a file `./infrastructure/variables.tfvars` with the following variables:
```hcl
webpubsub_tunnel_gateway = <tunnel_link>
```
- Ensure that you have a valid Azure subscription and run `az login` to login to Azure
- Run `terraform apply` to deploy the infrastructure (including test and prod environments)
- Rename `template-local.settings.json` in `functions/app` to `local.settings.json` and fill the following connection strings.
  - Run `terraform output -raw webpubsub_test_connection_string` in folder `./infrastructure` and fill in `WebPubSubConnectionString`
  - Run `terraform output -raw cosmos_test_connection_string` in folder `./infrastructure` and fill in `CosmosDBConnectionString`

## Run

## Working in dev mode
While developing all the following commands should be run in parallel in different terminals.

For webapp building and testing run
```sh
sbt devtest
```

For functions building and testing run
```sh
sbt functions
```

For running functions run in directory `functions/app`
```sh
fun start
```

For allowing the webpubsub to access the functions run with the `tunnel_link` subdomain from setup
```sh
lt --subdomain {sub_domain} --port 7071 --print-requests
```

Then open `localhost:12345` in your browser. 

## Tailwind intellisense
Guide written for visual studio code with tailwind css support for outwatch. 

Run
```sh
npm i
```

Install vscode plugin `Tailwind CSS IntelliSense` and paste into plugin configuration the following settings
```json
"tailwindCSS.includeLanguages": {
    "scala": "html"
},
"[scala]": {  
    "tailwindCSS.experimental.classRegex": [
        "\\bcls\\s*:=\\s*\"([^\"]*)\""
    ],
},
"editor.quickSuggestions": {
    "strings": true
}
```

## Useful tools
- [Postman](https://www.postman.com/)
- [Protoman](https://github.com/spluxx/Protoman)

# Misc
- For simplicity Tailwind is installed in (in seperate nodejs packages) `webapp/` and in `/`, because vs code `tailwind intellisense` plugin needs tailwind installed in root directory.