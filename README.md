[![Scala CI](https://github.com/phisn/local-rating/actions/workflows/main.yml/badge.svg?branch=main)](https://github.com/phisn/local-rating/actions/workflows/main.yml)
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

## Working in dev mode (windows)
Run
```sh
./dev.bat
```
This will launch azure functions in another cmd window. Stop dev mode by pressing `enter`.
## Working in dev mode (linux or mac)
Run

```sh
sbt dev
```

Run in another terminal in directory `functions/deploy`
```
fun start --java --cors *
```

Then open `http://localhost:12345` in your browser. The backend function w 

This sbt-task will start webpack dev server, compile your code each time it changes and auto-reload the page.  
Webpack dev server will stop automatically when you stop the `dev` task
(e.g by hitting `Enter` in the sbt shell while you are in `dev` watch mode).

# Deployment
For the deployment `Terraform` needs to be installed. 

In `infrastructure/` initialize the deployment with
```sh
terraform init
```
Create / update the infrastructure with
```sh
terraform apply
```

- Add output `api_key` in github as a action secret named `AZURE_STATIC_WEB_APP_TOKEN`.
- Secret for azure function has to be get manually. [Reference](https://github.com/marketplace/actions/azure-functions-action). Download `Go to azure portal -> func-backend -> Overview -> Get publish profile`. Copy the contents to github as a action secret named `AZURE_FUNCTIONAPP_PUBLISH_PROFILE`.

## Techstack
Core
  - Scala
  - Rescala
  - Outwatch
 
Infrastructure
  - Azure
  - Terraform
  - Github Actions

UI
  - Tailwind
  - Daisyui
