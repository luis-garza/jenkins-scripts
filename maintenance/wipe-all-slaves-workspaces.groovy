import hudson.model.*
import jenkins.model.*

def originalExecutors = [:]
Hudson hudson = Hudson.getInstance()

// Get parameters
def targetSlave = build.buildVariableResolver.resolve("targetSlave")
def minExecutors = build.buildVariableResolver.resolve("minExecutors").toInteger()

// Show targets
if (targetSlave == "") {
	println("Running maintenance task for ALL slaves")
} else {
	println("Running maintenance task for slave [" + targetSlave + "]")
}
println()

// Show initial number of executors per node
println("Initial executors per node")
for (Slave slave : hudson.getSlaves()) {
    println(" - " + slave.getDisplayName() + " [" + slave.getNumExecutors() + "]")
	originalExecutors.put(slave.getDisplayName(), slave.getNumExecutors())
}
println()

// Decrease number of executors
println("Decreasing number of executors to [" + minExecutors + "]")
for (Slave slave : hudson.getSlaves()) {
    if (targetSlave == "" || targetSlave == slave.getDisplayName()) {
		slave.setNumExecutors(minExecutors)
	}
}
hudson.setNodes(hudson.getNodes())
hudson.save()
println()

// Show current number of executors per node
println("Current executors per node")
for (Slave slave : hudson.getSlaves()) {
    println(" - " + slave.getDisplayName() + " [" + slave.getNumExecutors() + "]")
}
println()

// For each job
println("Deleting all jobs workspaces in all slaves")
for (job in Hudson.instance.items) {

		jobName = job.getFullDisplayName()
		println(" - Job [" + jobName + "]")

    // For each node
		for (node in hudson.getNodes()) {

			// Check the job is not building
			if (job.getClass() != org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject && !job.isBuilding()) {

				// Filter by node
				if (targetSlave == "" || targetSlave == node.getDisplayName()) {

					// Obtain workspace path in current node
					customWorkspace = job.getCustomWorkspace()
					if (customWorkspace == null) {
						workspacePath = node.getWorkspaceFor(job)
					} else {
						workspacePath = node.getRootPath().child(customWorkspace)
					}

					// Get rid of the workspace
					pathAsString = workspacePath.getRemote()
					if (workspacePath.exists()){
						workspacePath.deleteRecursive()

						println("    - Node [" + node.getDisplayName() + "] - Workspace deleted [" + pathAsString + "]")
					} else {
						println("    - Node [" + node.getDisplayName() + "] - Nothing to delete")
					}

				} else {
					println(" - Job running skipped [" + jobName + "]")
				}

			} else {
				println("    - Node [" + node.getDisplayName() + "] - Skipped")
			}

		}

}
println()

// Recover initial number of executors
println("Recovering initial number of executors")
for (Slave slave : hudson.getSlaves()) {
    if (targetSlave == "" || targetSlave == slave.getDisplayName()) {
		slave.setNumExecutors(originalExecutors.get(slave.getDisplayName()))
	}
}
hudson.setNodes(hudson.getNodes())
hudson.save()
println()

// Show final number of executors per node
println("Final executors per node")
for (Slave slave : hudson.getSlaves()) {
    println(" - " + slave.getDisplayName() + " [" + slave.getNumExecutors() + "]")
}
println()
