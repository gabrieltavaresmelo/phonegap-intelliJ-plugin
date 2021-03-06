package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.util.PhoneGapSettings;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * PhoneGapRunProfileState.java
 * <p/>
 * Created by Masahiro Suzuka on 2014/04/06.
 */
public class PhoneGapRunProfileState extends CommandLineState {

    private Project project;
    @SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
    private ExecutionEnvironment env;
    private PhoneGapRunConfiguration phoneGapRunConfiguration;

    public PhoneGapRunProfileState(Project project,
                                   @NotNull ExecutionEnvironment env,
                                   PhoneGapRunConfiguration phoneGapRunConfiguration) {
        super(env);
        this.project = project;
        this.env = env;
        this.phoneGapRunConfiguration = phoneGapRunConfiguration;
    }

    @NotNull
    @Override
    protected ProcessHandler startProcess() throws ExecutionException {
        String projectDir = this.project.getBasePath();
        if (PhoneGapSettings.PHONEGAP_PLATFORM_RIPPLE.equals(this.phoneGapRunConfiguration.getPlatform())) { // ripple emu
            // if server.js is missing
            // Create server.js
            try {
                File serverScript = new File(projectDir + "/" + "server.js");
                if (!serverScript.exists()) {
                    writeScript(serverScript);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            GeneralCommandLine commandLine = new GeneralCommandLine("node", "server.js");
            commandLine.setWorkDirectory(projectDir);
            return new OSProcessHandler(commandLine);
        } else { // Android or iOS
            GeneralCommandLine commandLine = new GeneralCommandLine(
                    phoneGapRunConfiguration.getExecutableType().getPath(),
                    phoneGapRunConfiguration.getCommand(),
                    phoneGapRunConfiguration.getPlatform());

            // Change working dir to project dir
            commandLine.setWorkDirectory(projectDir);

            return new OSProcessHandler(commandLine);
        }
    }

    private void writeScript(File serverScript) throws IOException {
      // TODO
      // Replace server.js to Gruntfile.js
        FileWriter fileWriter = new FileWriter(serverScript);
        try {
          fileWriter.write(
              "var http = require('http');\n" +
                  "var url = require('url');\n" +
                  "var path = require('path');\n" +
                  "var fs = require('fs');\n" +
                  "\n" +
                  "http.createServer(function(req, res) {\n" +
                  "  var webroot = 'www';\n" +
                  "  var uri = webroot + url.parse(req.url).pathname;\n" +
                  "  var fileName = path.join(process.cwd(), uri);\n" +
                  "\n" +
                  "  if (uri == 'www/') {\n" +
                  "    fileName = path.join(process.cwd(), 'www/index.html');\n" +
                  "  }\n" +
                  "\n" +
                  "  fs.readFile(fileName, function(err, file) {\n" +
                  "    if(err){\n" +
                  "      // 404\n" +
                  "      res.writeHead(404, {\"Content-Type\": \"text/plain\"});\n" +
                  "      res.write('404 not found\\n');\n" +
                  "      res.end();\n" +
                  "    } else {\n" +
                  "      // 200\n" +
                  "      var mimeType = 'text/plain';// Default mime-type\n" +
                  "      var extention = path.extname(fileName);\n" +
                  "      if (extention == '.html') {\n" +
                  "        mimeType = 'text/html';\n" +
                  "      } else if (extention == '.css') {\n" +
                  "        mimeType = 'text/css';\n" +
                  "      } else if (extention == '.js') {\n" +
                  "        mimeType = 'text/javascript';\n" +
                  "      }\n" +
                  "\n" +
                  "      res.writeHead(200, {\"Content-Type\": mimeType});\n" +
                  "      res.write(file);\n" +
                  "      res.end();\n" +
                  "    }\n" +
                  "  });\n" +
                  "\n" +
                  "}).listen(1337, '127.0.0.1');\n" +
                  "\n" +
                  "console.log('Server running at http://127.0.0.1:1337/');"
          );
        } finally {
          fileWriter.close();
        }
    }
}
