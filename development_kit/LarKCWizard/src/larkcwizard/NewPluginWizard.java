package larkcwizard;

import java.net.MalformedURLException;



import java.net.URL;

import larkcwizard.pages.LarkcMavenPluginPage;


import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;


public class NewPluginWizard extends Wizard implements INewWizard {

	private LarkcMavenPluginPage _pageOne;
	private static final String WINDOW_TITLE="LarKC plug-in Project Wizard";
	private static final String PAGE_TITLE="Create new LarKC Plug-in";
	private static final String MESSAGEBOX_MESSAGE="You have successfully created your LarKC plugin. \n" +
			"To open the project select File -> Import. \n"+
			"Then select General-> Existing project into workspace.\n"+
			"Then import your plugin.";
	private static final String MESSAGEBOX_TITLE = "Next Step";

	public NewPluginWizard() {
		setWindowTitle(WINDOW_TITLE);
	}

	@Override
	public void addPages() {
		super.addPages();

		_pageOne = new LarkcMavenPluginPage("newLarkcMavenPluginPage");

		_pageOne.setMessage("");
		URL url = null;
		try {
			url = new URL(larkcwizard.Activator.getDefault().getDescriptor()
					.getInstallURL(), "LarKCLogo.png");
			_pageOne.setImageDescriptor(ImageDescriptor.createFromURL(url));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		_pageOne.setTitle(PAGE_TITLE);
		
		
		addPage(_pageOne);
	}

	@Override
	public boolean performFinish() {

		
		
		
		IWorkspace workspace= ResourcesPlugin.getWorkspace(); 
		Shell shell= getShell();
		final IPath projectDirectoryPath =  workspace.getRoot().getRawLocation();
		String pluginName = _pageOne.getPluginName();
		IPath projectFilePath = projectDirectoryPath.append("\\"+pluginName+"\\.project");
		
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell); 
		try{
		dialog.run(true, true, new IRunnableWithProgress(){ 
		    public void run(IProgressMonitor monitor) { 
		        monitor.beginTask("Your plugin is being created and installed...", 100); 
		        LarKCMavenPluginProjectSupport.createMavenPlugin(
						_pageOne.getPluginName(), _pageOne.getArtifactId(),
						_pageOne.getVersionId(),projectDirectoryPath,monitor); 
		        monitor.done(); 
		    } 
		});
		
		

		//an attempt to import project 
		/*String error="";
		try{
		IProjectDescription description = ResourcesPlugin.getWorkspace()
				.loadProjectDescription(projectFilePath);
		IProject project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(description.getName());
		
		//doesn't work because project is in the workspace root
		
		project.create(description,null);
		project.open(null);
		}catch(Exception e)
		{
			error=" "+e.getMessage();
		}*/
		
		//Instructions for importing the plugin

		
		
		MessageBox messageBox =new MessageBox(shell,SWT.OK);
		messageBox.setText(MESSAGEBOX_TITLE);
		messageBox.setMessage(MESSAGEBOX_MESSAGE);
		messageBox.open();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		return true;
	}

	/*
	 * @Override public boolean canFinish(){ return true; }
	 */

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub

	}

}
