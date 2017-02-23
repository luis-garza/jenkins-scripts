// Define the target plugin
def pluginName = /com.xebialabs.xlrelease.ci.XLReleaseNotifier/

// For each FreeStyleProject job
for (item in jenkins.model.Jenkins.getInstance().getAllItems(hudson.model.FreeStyleProject)) {

	// Filter by plugin name
	if (item.getPublishersList().findAll{it =~ pluginName }) {

		// Get the target elements to transform
		println("Found job [" + item.getFullName() + "]")
		publisher = item.getPublishersList().find{ it.getClass() =~ pluginName }
		variableList = publisher.variables

		// For each element in the variable list
		for (var in varsList) {
			// Run the transformation
		}

		// Save the configuration
		publisher.variables = variableList
		item.save()
		println "Job configuration saved"
		println()

	}

}
