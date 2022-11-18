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
- Secret for azure function has to be get manually. [Reference](https://github.com/marketplace/actions/azure-functions-action). Download `Go to azure portal -> func-ratable-core -> Overview -> Get publish profile`. Copy the contents to github as a action secret named `AZURE_FUNCTIONAPP_PUBLISH_PROFILE`.

## Google SSO
Follow following tutorials
- https://developers.google.com/identity/gsi/web/guides/get-google-api-clientid
- https://learn.microsoft.com/en-us/azure/app-service/configure-authentication-provider-google

How it works
- https://learn.microsoft.com/en-us/azure/app-service/tutorial-auth-aad?pivots=platform-linux
