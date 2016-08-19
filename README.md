**Google Cardboard Benchmarks**

Google Cardboard and Google VR presents a new challenge to the User-Experience of Android devices. To render binocular video, the system must draw twice as many objects for any given scene. Furthermore, given the nature of VR technology, the impact of low frame rates and inconsistencies in frame rate(Jank) on the user experience is much larger than it is in other applications.

As the Cardboard SDK and the concept of VR experiences on mobile devices is a new one, there are currently few workloads testing the performance of mobile devices in a VR-specific application. This workload is designed to simulate a realistic use-case for a Google VR based game and records metrics important to a seamless VR experience for the end user.

**CubeTest Workload Overview**

CubeTest is designed to simulate a typical use case for the Android platform in a Google Cardboard VR application in a simple and repeatable workload. To mimic a typical Google Cardboard experience, the workload presents a relatively basic world view with an ever-increasing number of cube objects. While the cube objects do not do a great job of showing visually what a finished VR world might look like, they do a good job of simulating the load on the CPU and GPU that rendering these objects each frame exerts.

The workload relies on the OpenGL ES framework for 3D graphics and rendering of objects. Each object is redrawn each frame regardless of where the phone is actually &quot;looking,&quot; so the results of the workload are stable no matter what direction the phone is facing. This eliminates any user impact on the test results.

The primary limiting factor seems to be the number of animations/second that the device can handle.

**How my workload differs from the original:**

(differing code blocks, showing one cube allocated, showing new code allocating a bunch, text saying how I had to generalize the cube definition, maybe including filename)

**How to run CubeTest**

The CubeTest Performance Workload will start the application with one row of 11 cubes and will add a row of cubes every 5 seconds until the process hits a minimum average (over the 5 seconds) below 10 frames per second. Once the workload is complete, it will display the results. From the result screen, the user can either rerun the workload or download the results as a CSV file. The CSV file can be opened from the device in Google Sheets or downloaded to a PC for further analysis.

The CubeTest Battery Workload will start the application with a user-selected number of rows of cubes, run the workload at that

**CubeTest Performance Overview**

CubeTest is a heavy single-core workload. Only one thread is kept hot throughout the workload. The workload also shows very heavy utilization of the ART(Android Runtime). This is because OpenGL ES is making many JNI data copies under the surface. Specifically, in a VR workload, some data copy operations will be performed twice as often because some data is unique for each eye. Also, screen resolution does have an effect on devices&#39; performance on the workload. We noticed that scaling down certain device&#39;s resolution would cause them to perform better on the workload.

**Screenshots:**

View of the workload in action:



View of the results screen with explinations:

**Links:**

GoogleVR: https://vr.google.com/

GoogleVR SDK: https://github.com/googlevr/gvr-android-sdk/

GoogleVR SDK Samples: https://github.com/googlevr/gvr-android-sdk/tree/master/samples