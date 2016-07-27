package org.mfm;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import com.bmc.arsys.api.ARServerUser;
import com.bmc.arsys.api.Constants;
import com.bmc.arsys.api.ServerInfoMap;
import com.bmc.arsys.api.Value;

public class ServerLog {
	
	private String serverName;
	private int serverPort;
	private String userName;
	private String userPass;
	private int debugOnOptions = 0;
	private int debugOffOptions = 0;
	private String logFilename;
	private ServerInfoMap serverInfoLogFiles = new ServerInfoMap();
	
	public boolean ProcessArguments(String[] args){
		Options opts = new Options();
		opts.addOption(OptionBuilder.isRequired()
									.hasArg()
									.withArgName("server")
									.withDescription("ARS Server name")
									.withHelpOrder("1")
									.create("s"));
		
		opts.addOption(OptionBuilder.isRequired(false)
									.hasArg()
									.withArgName("port")
									.withDescription("Port number")
									.withHelpOrder("2")
									.create("x"));
		
		opts.addOption(OptionBuilder.isRequired()
									.hasArg()
									.withArgName("login")
									.withDescription("User name")
									.withHelpOrder("3")
									.create("u"));
		
		opts.addOption(OptionBuilder.isRequired()
									.hasArg()
									.withArgName("password")
									.withDescription("User password")
									.withHelpOrder("4")
									.create("p"));
		
		opts.addOption(OptionBuilder.isRequired(false)
									.hasArg(true)
									.withArgName("on,off")
									.withDescription("Enable or disable Escalation logs")
									.withHelpOrder("6")
									.create("esc"));
		
		opts.addOption(OptionBuilder.isRequired(false)
									.hasArg(true)
									.withArgName("on,off")
									.withDescription("Enable or disable Filter logs")
									.withHelpOrder("7")
									.create("flt"));
		
		opts.addOption(OptionBuilder.isRequired(false)
									.hasArg(true)
									.withArgName("on,off")
									.withDescription("Enable or disable API logs")
									.withHelpOrder("8")
									.create("api"));
		
		opts.addOption(OptionBuilder.isRequired(false)
									.hasArg(true)
									.withArgName("on,off")
									.withDescription("Enable or disable SQL logs")
									.withHelpOrder("9")
									.create("sql"));
		
		opts.addOption(OptionBuilder.isRequired(false)
									.hasArg(true)
									.withArgName("filename")
									.withDescription("Specify log file")
									.withHelpOrder("5")
									.create("f"));
		
		CommandLineParser parser = new BasicParser();
		CommandLine cmd;
		StringBuffer sb = new StringBuffer();

		try {	
			cmd = parser.parse(opts, args);
			serverName = cmd.getOptionValue("s");
			if(cmd.getOptionValue("x") != null)
				serverPort = Integer.valueOf(cmd.getOptionValue("x"));
			userName = cmd.getOptionValue("u");
			userPass = cmd.getOptionValue("p");
			logFilename = cmd.getOptionValue("f");
			
			if(cmd.hasOption("esc")) {
				switch(cmd.getOptionValue("esc")) {
					case "on":
						debugOnOptions |= Constants.AR_DEBUG_SERVER_ESCALATION;
						sb.append("Turning ON Escalation logging");
						if(logFilename != null) {
							serverInfoLogFiles.put(Constants.AR_SERVER_INFO_ESCALATION_LOG_FILE, new Value(logFilename));
							sb.append(", new log file - ").append(logFilename);
						}
						sb.append("\n");
						break;
					case "off":
						debugOffOptions |= Constants.AR_DEBUG_SERVER_ESCALATION;
						sb.append("Turning OFF Escalation logging\n");
						break;
					default:
						throw new Exception();
				}
			}
			if(cmd.hasOption("flt")) {
				switch(cmd.getOptionValue("flt")) {
					case "on":
						debugOnOptions |= Constants.AR_DEBUG_SERVER_FILTER;
						sb.append("Turning ON Filter logging");
						if(logFilename != null) {
							serverInfoLogFiles.put(Constants.AR_SERVER_INFO_FILTER_LOG_FILE, new Value(logFilename));
							sb.append(", new log file - ").append(logFilename);
						}
						sb.append("\n");
						break;
					case "off":
						debugOffOptions |= Constants.AR_DEBUG_SERVER_FILTER;
						sb.append("Turning OFF Filter logging\n");
						break;
					default:
						throw new Exception();
				}
			}
			if(cmd.hasOption("api")) {
				switch(cmd.getOptionValue("api")) {
					case "on":
						debugOnOptions |= Constants.AR_DEBUG_SERVER_API;
						sb.append("Turning ON API logging");
						if(logFilename != null) {
							serverInfoLogFiles.put(Constants.AR_SERVER_INFO_API_LOG_FILE, new Value(logFilename));
							sb.append(", new log file - ").append(logFilename);
						}
						sb.append("\n");
						break;
					case "off":
						debugOffOptions |= Constants.AR_DEBUG_SERVER_API;
						sb.append("Turning OFF API logging\n");
						break;
					default:
						throw new Exception();
				}
			}
			if(cmd.hasOption("sql")) {
				switch(cmd.getOptionValue("sql")) {
					case "on":
						debugOnOptions |= Constants.AR_DEBUG_SERVER_SQL;
						sb.append("Turning ON SQL logging");
						if(logFilename != null) {
							serverInfoLogFiles.put(Constants.AR_SERVER_INFO_SQL_LOG_FILE, new Value(logFilename));
							sb.append(", new log file - ").append(logFilename);
						}
						sb.append("\n");
						break;
					case "off":
						debugOffOptions |= Constants.AR_DEBUG_SERVER_SQL;
						sb.append("Turning OFF SQL logging\n");
						break;
					default:
						throw new Exception();
				}
			}
		}
		catch (Exception e) {
			HelpFormatter help = new HelpFormatter();
			help.printHelp("ServerLog [options], * - required option", opts);
			return true;
		}
		System.out.print(sb.toString());
		return false;
	}

	
	public void ProcessServerLog() {
		System.out.print("Executing...");
		try {
			ARServerUser ctx = new ARServerUser(userName, userPass, "", serverName, serverPort);
			ctx.verifyUser();
			int[] logFilesInfo = {Constants.AR_SERVER_INFO_DEBUG_MODE};
			ServerInfoMap serverInfo = ctx.getServerInfo(logFilesInfo);
			
			int currentDebugOptions = serverInfo.get(Constants.AR_SERVER_INFO_DEBUG_MODE).getIntValue();
			currentDebugOptions |= debugOnOptions;
			currentDebugOptions &= ~debugOffOptions;
			
			serverInfo.put(Constants.AR_SERVER_INFO_DEBUG_MODE, new Value(currentDebugOptions));
			
			serverInfo.putAll(serverInfoLogFiles);
					
			ctx.setServerInfo(serverInfo);

			System.out.print("done!\n");
		}
		catch(Exception e)
		{
			System.out.print("ERROR!!!\n");
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		ServerLog log = new ServerLog();
		if(log.ProcessArguments(args))
			return;
		log.ProcessServerLog();
		
	}
}
