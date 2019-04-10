package csironi.ggp.course.statsSummarizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.logging.GamerLogger;

import external.JSON.JSONArray;
import external.JSON.JSONException;
import external.JSON.JSONObject;

public class AddRolesToJson {


	/**
	 * Give n role names, and a folder adds to all .json files in the folder an entry with the role names.
	 *
	 * Example:
	 * INPUT: folderpath white;black
	 * Line added to each json file: "roles":["white","black"]
	 * @param args
	 */
	public static void main(String[] args) {

		if(args.length !=2) {
			System.out.println("Two inputs expected: folder path where to look for .json files and list of roles separated by semicolon.");
			return;
		}

		String folderpath = args[0];

		String[] roles = args[1].split(";");
		List<String> rolesForJson = new ArrayList<String>();
		for(String s : roles) {
			rolesForJson.add(s);
		}

		File folder;
		File[] filesInFolder;

		List<File> folders = new ArrayList<File>();
		folders.add(new File(folderpath));
		while(!folders.isEmpty()) {
			folder = folders.remove(0);
			//System.out.println(folder.getAbsolutePath());
			filesInFolder = folder.listFiles();
			for(File file : filesInFolder) {
				if(file.isDirectory()) {
					folders.add(file);
				}else if(file.getName().endsWith(".json")) {
					addRoles(file, rolesForJson);
				}
			}
		}
	}

	public static void addRoles(File jsonfile, List<String> roles) {

		BufferedReader br;
		String theLine;
		try {
			br = new BufferedReader(new FileReader(jsonfile));
			theLine = br.readLine();
			br.close();
		} catch (IOException e) {
			System.out.println("Exception when reading a .json file. File " + jsonfile.getAbsolutePath() + " not modified.");
        	e.printStackTrace();
        	return;
		}

		// Check if the file was empty.
		if(theLine == null || theLine.equals("")){
			System.out.println("Empty JSON file " + jsonfile.getAbsolutePath() + ".");
        	return;
		}

		JSONObject matchJSONObject = null;
        try{
        	matchJSONObject = new JSONObject(theLine);
        }catch(JSONException e){
        	System.out.println("Exception when parsing file to JSON: " + jsonfile.getAbsolutePath() + ".");
        	e.printStackTrace();
        }

        // Check if the JSON file already contains the roles.
        if(matchJSONObject.has("roles")){

        	System.out.println("JSON file " + jsonfile.getAbsolutePath() + " already containing roles.");

        	String playersRoles;
        	try{
            	JSONArray jsonroles = matchJSONObject.getJSONArray("roles");
            	playersRoles = "[ ";
            	for(int j = 0; j < jsonroles.length(); j++){
            		playersRoles += jsonroles.getString(j) + " ";
            	}
            	playersRoles += "]";
            }catch(JSONException e){
            	System.out.println("Information (\"roles\" array) improperly formatted in the JSON file " + jsonfile.getAbsolutePath() + ".");
            	e.printStackTrace();
            	return;
            }

        	System.out.println("Roles: " + playersRoles + ".");
        	return;
        }else {
        	try {
        		matchJSONObject.put("roles", roles);
        	} catch (JSONException e) {
        		return;
        	}

			if (jsonfile.exists()) jsonfile.delete();
			BufferedWriter bw;
			try {
				bw = new BufferedWriter(new FileWriter(jsonfile));
				bw.write(matchJSONObject.toString());
				bw.flush();
				bw.close();
			} catch (IOException e) {
				GamerLogger.logError("MatchRunner", "Match completed correctly, but impossible to save match information on JSON file.");
				GamerLogger.logStackTrace("MatchRunner", e);
			}
        }
	}

}
