*StreamSis*
==============
*Streamer's little helper*

What is it and what does it do
--------------
No explicit explanation here.  
*Recently I decided to fill up my GitHub profile and from now on I want my future commits to be publicly trackable.  
Still now I want any public attention to be less as possible.*  
But if you are a programmer, I'm sure you can figure it out. =)  

Not ready for release
--------------
Currently StreamSis is completelely unusable for the end user.  
GUI is under active development and few must-have things are not yet implemented.  
Because GUI is not ready, it's impossible to construct StreamSis's Projects directly in the running program.  
So, dear developer, if you want to test how StreamSis really works, you will need to hardcode your own Project. See ```Playground``` class for examples.  
- StreamSis runs on:
  - Windows: Yes
  - Linux: Not yet tested
  - Mac: Not yet tested

Not ready for accepting contributions
--------------
Currently StreamSis project is not accepting contributions.  
Contribution guidelines, final code style and CLA (Contributor license agreement) are still undefined.  

Working with the code
--------------
Dear developer, before you start, please make sure you have these things:
- Any IDE that supports Java 8 and Maven. For example:
  - Netbeans
  - IntelliJ IDEA
  - Eclipse with m2e plugin (my choice, fully supports Java 8 starting from version "Luna")
- Java SE Development Kit 8 (JDK 8). Any version above **8u60**.
  - Download and install it from the official site if you don't have it.
- Maven (3.x+). It's a build automation tool for Java. It automatically downloads depencencies for StreamSis and does some other nice things.
  - Download and install it from the official site if you don't have it. Please follow the **installation instructions**: https://maven.apache.org/install.html
- StreamSis source code repository. 
  - Download and unpack it somewhere on your computer. Or ```git clone``` it from the link using a program called Git.
- Basic knowledge for working with Maven.
  - Download and unpack it in your head. Here's the nice link: https://maven.apache.org/guides/getting-started/index.html
  - I really recommend you to take some time and get to know Maven. Maven is used in many Java projects, because it's so convenient.

Alright. Then:
- Execute the command ```mvn clean install``` in StreamSis repository directory. (**very important**) 
  - If you haven't executed this command before, Maven will start downloading modules and plugins for himself and libraries for StreamSis. **It might take some time.** 
- **Import** the StreamSis repository directory **as Maven project** into your favorite IDE that supports Java 8 and Maven. 
  - If you are using Eclipse IDE with m2e plugin for the first time, it will start to build cache intended to speed up searching Maven Artifacts. This cache will consume **1-3 GB of disk space** in your home directory. So be prepared. =)
- Ensure that your IDE is using Java Development Kit from above for the StreamSis project.
- Try to run the main class "com.ubershy.streamsis.StreamSis.java" from your IDE.
- And if it runs... Congratulations, you can start working with the code.

Assembling StreamSis.
--------------
Maven and JDK 8 are required.  
Execute ```build.sh``` or ```build.bat``` in the StreamSis repository directory.  
After that, if everything went smooth, you can find the assembled JARs with StreamSis and its dependencies in ```target/appassembler/repo``` subdirectory and platform-specific scripts for running StreamSis in ```target/appassembler/bin``` subdirectory.