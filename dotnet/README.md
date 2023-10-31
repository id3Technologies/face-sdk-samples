# Face SDK .NET samples

This repository contains Microsoft .NET samples of **id3 Technologies** Face SDK.

## Requirements

First of all you must follow the upper README steps to get a license file and install the SDK.

.NET samples require **Microsoft Visual Studio 2017** (or more recent) to be installed on your PC.

Once everything is setup you can proceed to the following steps.

## Building the solution

Open the solution file **id3.Face.Samples.sln** with **Microsoft Visual Studio**.

### Setting references

The reference to id3.Face package should already be set up in each project.

Also, for the Windows Form projects, references to **OpenCvSharp** and **OpenCvSharp.Extensions** should be set up as well to handle a webcam. The IDE should offer you to download the Nuget packages if necessary. Follow its instructions.

### Filling the license path

Before to build any of the .NET samples, you need to fill in the path to your license in the source code. Look for the following line in **Program.cs** or **Form1.cs** and add your correct path :

    string licensePath = Environment.GetEnvironmentVariable("ID3_LICENSE_PATH");
    
You may also set the environment variable : `export ID3_LICENSE_PATH=<path/to/license.lic>`

Once everything is ready, you can now build the samples and launch them.