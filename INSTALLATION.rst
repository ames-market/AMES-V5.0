
============
Installation
============

AMES V5.0 is only supported on Windows Platform. 

1. 	Install Java from https://www.oracle.com/technetwork/java/javase/downloads/index.html

   	After Java is installed, add 'LocationToJavaDirectory/bin' ( e.g. C:\Java\jdk-13.0.2\bin) to the PATH system variable.
   
  	As AMES V5.0 is integrated with FNCS, running AMES V5.0 requires FNCS dependencies. 
   
   	The FNCS dependencies uploaded as part of this repository need to be downloaded, and their location needs to be added to the PATH system variable. 

   	Add an environmental variable JAVA_HOME with the above 'LocationToJavaDirectory' (e.g. JAVA_HOME is set to C:\Java\jdk-13.0.2). This is required for running ANT.
	
   	Verify Java installation using "Java -version" command prompt.  
   

2.	The ANT tool used to compile AMES V5.0. ANT must be downloaded and extracted to a local directory.

    	Download Apache Ant from: https://ant.apache.org/
	
	Extract the zip file into any directory.
	
	Set ANT_HOME environmental variable to the above directory.
	
	Include ANT_HOME/bin in the PATH system variable.
	
	Verify installation using "ant -version" command prompt.  
    
	
3.	Install NetBeans IDE from https://netbeans.apache.org/download/ 
	
	NetBeans IDE is useful in resolving build errors with ant (if any). 
	
4.	Python

    	AMES V5.0 uses modified Power System Simulation Toolbox (PSST), based on Python (V3). Thus, Python must first be locally installed. 
    
    	Python can be installed using any of the following choices:
    
    	Choice 1: Install Python using the Anaconda Distribution, available for downloading from https://www.anaconda.com/distribution/
	Check https://docs.anaconda.com/anaconda/install/windows/ for installation instructions. 

    	Choice 2: Install Python using the Miniconda installer following the instructions given at https://conda.io/miniconda.html 
	Note: Pay particular attention to how the conda package manager is used to install various required modules such as numpy. 

    	Choice 3: Install standard Python from https://www.python.org/ . The optional ‘pip’ is needed to install modules such as numpy.
	Note: The current study used the Miniconda installer from https://docs.conda.io/en/latest/miniconda.html to install Python (V3) by following
	the instructions given at TESP website (link: https://tesp.readthedocs.io/en/latest/Installing_Windows_Link.html) at the location C:\Miniconda3

	Add C:/Miniconda3 to path (python.exe is located at C:\Miniconda3) to recognize Python from cmd (or powershell) else only conda prompt knows Python.
	
	Add C:/Miniconda3/Scripts and C:Miniconda3/Library/bin to use conda to install packages.

	Verify installation using "Python --version" command prompt.  
	
	Verify access to pip and conda (by typing pip/conda).
	
	To install modules, use 'pip install ModuleName' or 'conda install ModuleName'.
	
	To uninstall modules, use 'pip uninstall ModuleName' or 'conda uninstall ModuleName'.

        Note: For “version” command line prompts, Python requires the use of a double hyphen “- -version” whereas Ant requires the use of a single hyphen “-version”.  For Java, either option works.



5. 	Install psst.

    	After Python has been locally installed, PSST must be locally installed. PSST has been uploaded as part of AMES V5.0.  Therefore, PSST will automatically download as part of the AMES V5.0 download.
    
    	After PSST has been downloaded to a local folder, it can be installed from the command line for this local folder in two steps, as follows:  
  	
	Step 1. Navigate to the psst folder using: 
	
	cd C:/YourlocationtoAMESV5/psst
	
	Step 2. Install PSST by means of the following pip install command:
	
	pip install -e .
    
    	Note:  The pip install command “pip install -e .” in Step 2 has a period “.” at the end. Also, PSST has its own dependencies, which are installed when the pip install command is given.
    
   
6. 	Solver

    	AMES V5.0 uses the CPLEX optimization solver, available at: https://www.ibm.com/support/pages/downloading-ibm-ilog-cplex-optimization-studio-v1290
    
