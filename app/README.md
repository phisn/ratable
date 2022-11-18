# Development
## Prerequisites
You should make sure that the following components are pre-installed on your machine:
 - [Node.js](https://nodejs.org/en/download/)
 - [Yarn](https://yarnpkg.com/en/docs/install)
 - JDK 11+
 - sbt

## Prepare tailwind intellisense
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

## Provide Azure services for local azure functions
See [Link](https://github.com/Azure/azure-webpubsub/tree/main/samples/functions/js/simplechat) for more information.

- Install `npm install -g localtunnel`
- Run `lt --port 7071 --print-requests` output needed later as `tunnel_link`

To run the functions locally we need access to some Azure services. Currently no custom dev infrastructure exist, so we use the infrastructure from [Deployment](#Deployment). Rename `template-local.settings.json` in `functions/app` to `local.settings.json` and fill the following connection strings.

- Fill connectionstring from `terraform output -raw webpubsub_test_connection_string` in `WebPubSubConnectionString`
- We need to register a upstream webhook
  - Go to azure portal `webpubsub -> settings -> +add`
  - Hub name `distribution`
  - Add Event handler with URL `{tunnel_link}/runtime/webhooks/webpubsub` with system events `connect, connected, disconnected` and user events `all`

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

For running functions run  in directory `functions/app`
```sh
fun start
```

For allowing the webpubsub to access the functions run with the subdomain from [`tunnel_link`](#Provide-Azure-services-for-local-azure-functions)
```sh
lt --port 7071 --print-requests
lt --subdomain {sub_domain} --port 7071 --print-requests
```
The domain has to be registered as a eventhandler in the azure portal in the webpubsub.

Then open `localhost:12345` in your browser. 


## Useful tools
- [Postman](https://www.postman.com/)
- [Protoman](https://github.com/spluxx/Protoman)

# Misc
- For simplicity Tailwind is installed in (in seperate nodejs packages) `webapp/` and in `/`, because vs code `tailwind intellisense` plugin needs tailwind installed in root directory.