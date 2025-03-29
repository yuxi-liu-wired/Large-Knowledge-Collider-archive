package larkcwizard;

//import java.io.File;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.internal.resources.Folder;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.ui.internal.ide.undo.FolderDescription;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.debug.ui.DebugUITools;

/**
 * This class generates the Project
 * 
 * @author Luka,Janez
 * 
 */
public class LarKCMavenPluginProjectSupport {

	

	public static void createMavenPlugin(String pluginName,
			String artifactId, String versionId, IPath workspacePath, IProgressMonitor monitor) {
		
		monitor.subTask("Creating the plugin");
		String createCommand = "cmd"
				+ " /C"
				+ " mvn"
				+ " archetype:generate"
				+ " -DarchetypeCatalog=http://larkc.svn.sourceforge.net/svnroot/larkc/trunk/larkc-plugin-archetype/"
				+ " -DarchetypeGroupId=eu.larkc"
				+ " -DarchetypeArtifactId=larkc-plugin-archetype "
				+ " -DarchetypeVersion=0.0.13" + " -DgroupId=eu.larkc"
				+ " -DartifactId=" + artifactId + " -Dversion=" + versionId
				+ " -Dpackage=eu.larkc.plugin" + " -DlarkcPluginName="
				+ pluginName + " -DinteractiveMode=false";
		
		executeCommand(createCommand,workspacePath.toFile());
		monitor.worked(50);
		monitor.subTask("Installing the plugin");
		IPath projectDirectory = workspacePath.append("\\"+pluginName);
		String installCommand = "cmd"
				+ " /C"
				+ " mvn"
				+ " install";
		executeCommand(installCommand,projectDirectory.toFile());
		monitor.worked(100);
	}
	
	public static String executeCommand(String command, File path) {
		try {
			
			Process process = Runtime.getRuntime().exec(command, null, path);
			InputStream inputStream = process.getInputStream();
			InputStream errorStream = process.getErrorStream();
			BufferedReader bufferedInput = new BufferedReader(
					new InputStreamReader(inputStream));
			BufferedReader bufferedError = new BufferedReader(
					new InputStreamReader(errorStream));
			StringBuffer message = new StringBuffer();
			
			while (true) {
				if (errorStream.available() > 0) {
					String lineIn;
					while ((lineIn = bufferedError.readLine()) != null) {
						message.append("ERROR: ");
						message.append(lineIn);
						message.append(System.getProperty("line.separator"));
					}
				}
				if (inputStream.available() > 0) {
					String lineIn;
					while ((lineIn = bufferedInput.readLine()) != null) {
						message.append(lineIn);
						message.append(System.getProperty("line.separator"));
					}
				}
				
				try {
					process.exitValue();
					break;
				} catch (Throwable throwable) {
					Thread.sleep(1000);
				}
			}
			if (message.length() > 0) {
				// System.out.println(message);
			}
			bufferedInput.close();
			bufferedError.close();
			return message.toString();
		} catch (Throwable throwable) {
			System.out.println(throwable.getMessage());
		}
		return null;

	}

}
