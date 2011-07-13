package org.jboss;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.jboss.jmx.adaptor.snmp.generator.Parser;
import org.jboss.jmx.adaptor.snmp.generator.Generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Goal which generates an MIB from given xml files
 *
 * @goal generate-mib
 * 
 */
public class MibGeneratorMojo
    extends AbstractMojo
{
     /**
     * @parameter
     */
    private String attributesFile;

    /**
     * @parameter
     */
    private String notificationFile;

    /**
     * @parameter
     * @required
     */
    private String outputFile;

    /**
     * @parameter
     * @required
     */
    private String moduleName;

    public void execute()
        throws MojoExecutionException
    {
	getLog().info("Generating MIB from attributes file = "+attributesFile+" and notifications file = "+notificationFile);
	Parser parser = new Parser(attributesFile, notificationFile);
	getLog().info("Parsing...");
	parser.parse();
	getLog().info("Writing file "+outputFile+"...");
	Generator generator = new Generator(outputFile, moduleName, parser.getMaList(), parser.getMbList(), parser.getNmList());
	generator.writeFile();
    }
}
