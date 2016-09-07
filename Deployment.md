# Deploying Scala Play Framework Applications on AWS Beanstalk using Docker containers

## Overview
1. [Pre-requisites](#pre)
2. [Run your app in a local container](#local)
3. [Stage your app](#stage)
4. [Setup AWS EB CLI](#setup)
5. [Deploy](#deploy)

## <a name="pre"></a>Pre-requisites

- Have [Activator](https://www.lightbend.com/activator/download) installed
> Extract zip and add the bin folder to your path

- Have [AWS Elastic Beanstalk](http://docs.aws.amazon.com/elasticbeanstalk/latest/dg/Welcome.html) CLI installed
> `pip install awsebcli`

- Have [Docker](https://docs.docker.com/engine/installation/) installed

- Your [Play](https://www.playframework.com/) application
> Test your app to make sure it is ready for deployment. Run it locally using `activator run` which should make it available on `http://localhost:9000`

**Note:** Do not use `activator run` in deployment containers, it is not meant for deployment and will not work.

## <a name="local"></a>Run your app in a local container

Enter `activator docker:publishLocal` to automatically configure a local container image. Then run your container using `docker run -p <host_port>:9000 <image_name>` where host port is the TCP port you want to map the server to run on from the container port which should be 9000 by default. If you don't know the name of your image, run `docker images`.

Please note that this command will make the app listen on network all interfaces (aka `0.0.0.0`) Please ensure you're firewalled off from any non-trusted networks before you run it.

Visit `http://localhost:<host_port_from_earlier>` and make sure the app is working properly.

## <a name="stage"></a>Stage your app

Run `activator docker:stage` to package your app and all the necessary resources as well as generate a Dockerfile that can be used to build the Docker container. These resources are stored in `target/docker/stage`. Copy that entire folder to a location outside of the source tree (i.e. a location where the folder would not be a child of the root folder of the app source) such as `~` or `/tmp`.

## <a name="setup"></a>Setup AWS EB CLI

1. Run `eb init` and select which region you would like to deploy to.
2. Provide your AWS access keys (if you dont have any, go get [them](http://docs.aws.amazon.com/general/latest/gr/aws-sec-cred-types.html#access-keys-and-secret-access-keys)).
3. Create a new application and select "Docker" as your platform (pick the latest version if asked).
4. (Optional) If you want to set up ssh for EC2 Instances, go ahead and do that now.

## <a name="deploy"></a>Deploy

Make sure your current directory is the `stage` folder that was copied earlier and edit the "Dockerfile" to include the following line:

`EXPOSE 9000`

Elastic Beanstalk will scan the "Dockerfile" for that line in order to know which port to map to port 80 of the public server.

Finally, run the command `eb create` and name your environment. `eb create` can be configured with all kinds of options such as disabling load balancing, different EC2 Instance tiers, and more. Check out the AWS EB CLI [documentation](http://docs.aws.amazon.com/elasticbeanstalk/latest/dg/eb3-create.html) for more information.

The deployed app should be up and running at the CNAME URL output by the `eb create` command within about 10 minutes.
