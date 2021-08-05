FROM jenkins/jenkins

USER root

RUN apt-get update
RUN apt-get install python3 python3-distutils python3-apt -y
RUN curl -O https://bootstrap.pypa.io/get-pip.py
RUN python3 get-pip.py
RUN pip3 install ansible --upgrade 
