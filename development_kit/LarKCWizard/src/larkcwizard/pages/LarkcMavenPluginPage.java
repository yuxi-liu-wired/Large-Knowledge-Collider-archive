package larkcwizard.pages;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * This class generates the only page in the wizard
 * 
 * @author Janez
 * 
 */
public class LarkcMavenPluginPage extends WizardPage {

	protected String pluginName;
	protected String artifactId;
	protected String versionId;

	public LarkcMavenPluginPage(String pageName) {
		super(pageName);
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		setPageComplete(false);

		new Label(composite, SWT.LEFT).setText("Plugin Name:");
		final Text pluginNameText = new Text(composite, SWT.BORDER);
		pluginNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		

		new Label(composite, SWT.LEFT).setText("Artifact Id:");
		final Text artifactIdText = new Text(composite, SWT.BORDER);
		artifactIdText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(composite, SWT.LEFT).setText("Version Id:");
		final Text versionIdText = new Text(composite, SWT.BORDER);
		versionIdText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		versionIdText.setText("0.0.1");
		versionId=versionIdText.getText();
		

		pluginNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				pluginName = pluginNameText.getText();
				setPageComplete(pluginName.length() > 0
						&& artifactId.length() > 0 && versionId.length() > 0 );
			}
		});
		artifactIdText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				artifactId = pluginNameText.getText();
				setPageComplete(pluginName.length() > 0
						&& artifactId.length() > 0  && versionId.length() > 0 );
			}
		});
		versionIdText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				pluginName = pluginNameText.getText();
				setPageComplete(pluginName.length() > 0
						&& artifactId.length() > 0  && versionId.length() > 0 );
			}
		});
		versionIdText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				versionId = pluginNameText.getText();
				setPageComplete(pluginName.length() > 0
						&& artifactId.length() > 0  && versionId.length() > 0 );
			}
		});
		setControl(composite);

	}

	public String getPluginName() {
		return pluginName;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getVersionId() {
		return versionId;
	}

	public void setVersionId(String versionId) {
		this.versionId = versionId;
	}

}
