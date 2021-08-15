This is a personal project of mine to test the Android environment:



This is a Kotlin Android App with the following tech-stack:

- MVI Architecture
- Hilt
- Kotlin Coroutines
- Kotlin's Channel and Flows
- Retrofit
- Firebase (Auth and DB)
- Room 
- Navigation
- Testing (Espresso, Mockito)
- Jenkins and Docker


I've developed 2 DevOps jobs to handle testing and automated Google Play upload triggered by a Github Webhook in Jenkins.


For questions and more info please contact leschinskysergey@gmail.com

Environment setup tutorial:

A few dependencies are required in order to run the job normally:

- Python3(3.6.9)
- Docker(20.10.7)


Run the following command to download and start the docker image:

	docker pull xmartlabs/jenkins-android
	docker run -p 8080:8080 -p 50000:50000 xmartlabs/jenkins-android

After the image has been successfully loaded, Docker will run with a certain container ID:
setup the jenkins environment and restore backup folder via Jenkin's ThinBackup Plugin



After you've restored your job configuration, run your container's server by invoking:
	
	docker exec -it -u root <container ID> bash 

You can find your container ID by running:

	docker ps


On it, configure the following dependencies:

- java11(11.0.12)
- Download Android's command line tools following this guide: https://stackoverflow.com/questions/34556884/how-to-install-android-sdk-on-ubuntu
- Install your wanted  Generic Emulator with Google Apis following this guide: https://gist.github.com/mrk-han/66ac1a724456cadf1c93f4218c6060ae


In order to trigger the Github WebHooks, an Ngrok server needs to be live,
you can set it up by using the ngrok folder and run:

	./ngrok http 8080


Then take the HTTP url and update the webhook to that url.


QnA:
Q: ProbeKVM: This user doesn't have permissions to use KVM
A: run the following command under root

    chown jenkins -R /dev/kvm

