package tests;

import net.devtech.filepipeline.api.VirtualDirectory;
import net.devtech.filepipeline.api.VirtualFile;
import net.devtech.filepipeline.api.VirtualPath;
import net.devtech.filepipeline.api.source.VirtualSink;
import net.devtech.filepipeline.api.source.VirtualSource;
import net.devtech.filepipeline.impl.util.FPInternal;

public class FPTests {
	public static void main(String[] args) {
		try(VirtualSink output = VirtualSink.workingDirectory(); // opens the working directory as an output
		    VirtualSource source = output.getSource()) { // view of the directory

			VirtualDirectory outputDirectory = output.outputDir("test/output"); // where to output
			VirtualFile testJar = source.resolveFile("test/test.jar"); // the test jar in the test directory
			VirtualSource testJarContents = testJar.openAsSource(); // the inside of the jar file as a VirtualSource (more powerful directory)
			testJarContents.depthStream().map(VirtualPath::relativePath).forEach(System.out::println);
			VirtualFile file = testJarContents.resolveFile("net/devtech/filepipeline/api/source/VirtualSource.class"); // find class file in jar
			VirtualFile to = output.outputFile(outputDirectory, "VirtualSource.class"); // "create" file to
			output.copy(file, to); // copy file out
		} catch(Exception e) {
			throw FPInternal.rethrow(e);
		}
	}
}
