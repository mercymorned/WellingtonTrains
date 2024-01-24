import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import ecs100.UI;
import ecs100.UIFileChooser;

/**
 * WellingtonTrains class maintains the UI for the Wellington Trains program.
 * <p>
 * The Wellington Trains class launches an interactive program that loads train
 * station, line, and services data for the Wellington region.
 *
 * @author Ronan Griffin griffirona@myvuw.ac.nz
 * @version 1.1
 */

public class WellingtonTrains {
	private Map<String, Station> stations = new HashMap<String, Station>();
	private List<TrainService> trainServices = new ArrayList<TrainService>();
	private Map<String, TrainLine> trainLines = new HashMap<String, TrainLine>();
	private List<Rectangle> shapes = new ArrayList<>();
	private double x;
	private double y;
	private double w;
	private double h;
	private double x1;
	private double y1;

	/**
	 * WellingtonTrains method establishes the UI buttons available to the user,
	 * sets a default window size with a background image, and begins the Mouse
	 * Listener action.
	 */
	public WellingtonTrains() {
		UI.initialise();
		UI.setMouseListener(this::doMouse);
		// standard size that fully displays the map image on launch
		UI.setWindowSize(1350, 750);
		UI.setDivider(0.37);
		// calls method to open region map in graphics pane on start
		loadWellyMap();
		/*
		 * Two lines below belong to out-dated method that had users load their own data
		 */
//		UI.addButton("Load station data file", this::loadStationData);
//		UI.addButton("Load train line data file" , this::loadTrainLineData);
		UI.addButton("Display all Wellington Region stations", this::printStationData);
		UI.addButton("Display all Wellington Region train lines", this::printLineData);
		UI.addButton("Display train lines available at each station", this::tlByStation);
		UI.addButton("Display all stations by train line", this::stationByTL);
		UI.addButton("Display lines available at specific station", this::searchStation);
		UI.addButton("Display all stations visited by a specific line", this::searchTL);
		UI.addButton("Display services on train line", this::listTLService);
		UI.addButton("Display service times at station", this::findStationSvcs);
		UI.addButton("Plan route between two stations", this::routePlan);
		UI.addButton("Show interactive system map", this::loadSystemMap);
		UI.addButton("Show Wellington Region map", this::loadWellyMap);

		// set up scanner for Station HashMap to load on program start
		try {
			String fileName = "stations.data";
			File stationData = new File(fileName);
			Scanner sc = new Scanner(stationData);
			while (sc.hasNext()) {
				String name = sc.next();
				int zone = sc.nextInt();
				double distance = sc.nextDouble();
				sc.nextLine();
				Station s = new Station(name, zone, distance);
				stations.put(name, s);
			}
			UI.println("Train Stations data succesfully loaded!");
		} catch (IOException ex) {
			UI.printf("Error loading file", ex);
		}
		// set up scanner for TrainLine HashMap to load on program start
		try {
			String fileName = "train-lines.data";
			File trainLineData = new File(fileName);
			Scanner sc = new Scanner(trainLineData);
			while (sc.hasNext()) {
				String name = sc.nextLine();
				TrainLine t = new TrainLine(name);
				trainLines.put(name, t);

				// Scanner for stations data
				String fN = name + "-stations.data";
				// console message to confirm files are being loaded
				System.out.println(fN + " loaded!");
				File TLStation = new File(fN);
				Scanner stationScan = new Scanner(TLStation);
				while (stationScan.hasNext()) {
					String stationName = stationScan.nextLine();
					t.addStation(stations.get(stationName));
					stations.get(stationName).addTrainLine(t);
				}
				// test code to confirm in console that items were being loaded
//				System.out.println(t.getStations());
//				System.out.println("***");
//				for (String sts : stations.keySet())
//				System.out.println(stations.get(sts).getTrainLines());
				stationScan.close();
				// set up scanner for Train Service loading
				String fN2 = name + "-services.data";
				// console message to confirm files are being loaded
				System.out.println(fN2 + " loaded!");
				File stationSvcs = new File(fN2);
				Scanner svcScan = new Scanner(stationSvcs);
				while (svcScan.hasNext()) {
					int time = svcScan.nextInt();
					TrainService ts = new TrainService(t);
					trainServices.add(ts);
					t.addTrainService(ts);
					ts.addTime(time, true);
				}
				// test code confirming in console that items were being loaded
				// System.out.println(t.getTrainServices());
			}
			UI.println("Train Lines data successfully loaded!");
		} catch (IOException ex) {
			ex.printStackTrace();
			UI.printf("Error loading file", ex);
		}
	}

	/**
	 * printStationData method prints an alphabetized list of all the region's
	 * stations into the text pane.
	 */
	public void printStationData() {
		UI.clearText();
		// using SortedSet to display station names alphabetically
		SortedSet<String> sortedStations = new TreeSet<String>(stations.keySet());
		for (String stN : sortedStations) {
			UI.println(stations.get(stN).toString());
		}
	}

	/**
	 * printLineData method prints an alphabetized list of the different train lines
	 * available in the system.
	 */
	public void printLineData() {
		UI.clearText();
		SortedSet<String> sortedLines = new TreeSet<String>(trainLines.keySet());
		for (String lineName : sortedLines) {
			UI.println(trainLines.get(lineName).toString());
			UI.println();
		}
	}

	/**
	 * tlByStation method prints an alphabetized list of train stations along with
	 * each line available at that station.
	 */
	public void tlByStation() {
		UI.clearText();
		SortedSet<String> sortedStations = new TreeSet<String>(stations.keySet());
		for (String sts : sortedStations) {
			UI.println(stations.get(sts));
			UI.println(stations.get(sts).getTrainLines().toString());
			UI.println();
		}
	}

	/**
	 * stationByTL method prints an alphabetized list of train lines along with the
	 * stations available on that line.
	 */
	public void stationByTL() {
		UI.clearText();
		SortedSet<String> sortedLines = new TreeSet<String>(trainLines.keySet());
		for (String tls : sortedLines) {
			UI.println(trainLines.get(tls));
			UI.println(trainLines.get(tls).getStations().toString());
			UI.println();
		}
	}

	/**
	 * searchStation method allows the user to select a station from a drop down
	 * list of all station names, then display the train lines available at that
	 * station.
	 */
	public void searchStation() {
		UI.clearText();

// initial code I started with: print list of stations and let user type name in
//		UI.askString
//		UI.println("List of station names:");
//		for (String s : stations.keySet()) {
//			UI.println(stations.get(s));
//		}
//		String stationName = UI.askString("Enter station name");
		Object[] stPossibleNames = { "Ava", "Awarua-Street", "Box-Hill", "Carterton", "Crofton-Downs", "Epuni",
				"Featherston", "Heretaunga", "Johnsonville", "Kenepuru", "Khandallah", "Linden", "Mana", "Manor-Park",
				"Masterton", "Matarawa", "Maymorn", "Melling", "Naenae", "Ngaio", "Ngauranga", "Paekakariki",
				"Paraparaumu", "Paremata", "Petone", "Plimmerton", "Pomare", "Porirua", "Pukerua-Bay", "Raroa",
				"Redwood", "Renal-Street", "Silverstream", "Simal-Crescent", "Solway", "Taita", "Takapu-Road", "Tawa",
				"Trentham", "Upper-Hutt", "Waikanae", "Wallaceville", "Waterloo", "Wellington", "Western-Hutt",
				"Wingate", "Woburn", "Woodside" };
		Object stationName = JOptionPane.showInputDialog(null, "Select one", "Input", JOptionPane.INFORMATION_MESSAGE,
				null, stPossibleNames, stPossibleNames[0]);
		UI.clearText();
		UI.println(stations.get(stationName));
		UI.println(stations.get(stationName).getTrainLines());
	}

	/**
	 * searchTL method lets user select train line from drop down and displays the
	 * stations available on that line.
	 */
	public void searchTL() {
		UI.clearText();
		Object[] tlPossibleNames = { "Johnsonville_Wellington", "Masterton_Wellington", "Melling_Wellington",
				"Upper-Hutt_Wellington", "Waikanae_Wellington", "Wellington_Johnsonville", "Wellington_Masterton",
				"Wellington_Melling", "Wellington_Upper-Hutt", "Wellington_Waikanae" };
		Object tlName = JOptionPane.showInputDialog(null, "Select one", "Input", JOptionPane.INFORMATION_MESSAGE, null,
				tlPossibleNames, tlPossibleNames[0]);
		UI.clearText();
		UI.println(trainLines.get(tlName));
		UI.println(trainLines.get(tlName).getStations());
	}

	/**
	 * listTLService allows the user to select a train line from drop down and
	 * displays service times on that line.
	 */
	public void listTLService() {
		UI.clearText();
		Object[] tlPossibleNames = { "Johnsonville_Wellington", "Masterton_Wellington", "Melling_Wellington",
				"Upper-Hutt_Wellington", "Waikanae_Wellington", "Wellington_Johnsonville", "Wellington_Masterton",
				"Wellington_Melling", "Upper-Hutt_Wellington", "Wellington_Waikanae" };
		Object tlName = JOptionPane.showInputDialog(null, "Select one", "Input", JOptionPane.INFORMATION_MESSAGE, null,
				tlPossibleNames, tlPossibleNames[0]);
		UI.clearText();
		UI.println(trainLines.get(tlName));
		UI.println(trainLines.get(tlName).getTrainServices().toString());
	}

	/**
	 * findStationSvcs allows the user to select a station from the drop down menu
	 * and display each train line that runs through that station. For each train
	 * line, displays the service times.
	 */
	public void findStationSvcs() {
		UI.clearText();
		Object[] stPossibleNames = { "Ava", "Awarua-Street", "Box-Hill", "Carterton", "Crofton-Downs", "Epuni",
				"Featherston", "Heretaunga", "Johnsonville", "Kenepuru", "Khandallah", "Linden", "Mana", "Manor-Park",
				"Masterton", "Matarawa", "Maymorn", "Melling", "Naenae", "Ngaio", "Ngauranga", "Paekakariki",
				"Paraparaumu", "Paremata", "Petone", "Plimmerton", "Pomare", "Porirua", "Pukerua-Bay", "Raroa",
				"Redwood", "Renal-Street", "Silverstream", "Simal-Crescent", "Solway", "Taita", "Takapu-Road", "Tawa",
				"Trentham", "Upper-Hutt", "Waikanae", "Wallaceville", "Waterloo", "Wellington", "Western-Hutt",
				"Wingate", "Woburn", "Woodside" };
		Object stationName = JOptionPane.showInputDialog(null, "Select one", "Input", JOptionPane.INFORMATION_MESSAGE,
				null, stPossibleNames, stPossibleNames[0]);
		if (stationName.equals("Ava")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Wellington_Upper-Hutt").getTrainServices());
			UI.println(trainLines.get("Upper-Hutt_Wellington").getTrainServices());
		} else if (stationName.equals("Awarua-Street")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Johnsonville_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Johnsonville").getTrainServices());
		} else if (stationName.equals("Box-Hill")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Johnsonville_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Johnsonville").getTrainServices());
		} else if (stationName.equals("Carterton")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Masterton_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Masterton").getTrainServices());
		} else if (stationName.equals("Crofton-Downs")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Johnsonville_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Johnsonville").getTrainServices());
		} else if (stationName.equals("Epuni")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Upper-Hutt_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Upper-Hutt").getTrainServices());
		} else if (stationName.equals("Featherston")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Masterton_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Masterton").getTrainServices());
		} else if (stationName.equals("Heretaunga")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Upper-Hutt_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Upper-Hutt").getTrainServices());
		} else if (stationName.equals("Johnsonville")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Johnsonville_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Johnsonville").getTrainServices());
		} else if (stationName.equals("Kenepuru")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Waikanae_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Waikanae").getTrainServices());
		} else if (stationName.equals("Khandallah")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Johnsonville_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Johnsonville").getTrainServices());
		} else if (stationName.equals("Linden")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Waikanae_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Waikanae").getTrainServices());
		} else if (stationName.equals("Mana")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Waikanae_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Waikanae").getTrainServices());
		} else if (stationName.equals("Manor-Park")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Upper-Hutt_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Upper-Hutt").getTrainServices());
		} else if (stationName.equals("Masterton")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Masterton_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Masterton").getTrainServices());
		} else if (stationName.equals("Matarawa")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Masterton_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Masterton").getTrainServices());
		} else if (stationName.equals("Maymorn")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Masterton_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Masterton").getTrainServices());
		} else if (stationName.equals("Melling")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Melling_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Melling").getTrainServices());
		} else if (stationName.equals("Naenae")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Upper-Hutt_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Upper-Hutt").getTrainServices());
		} else if (stationName.equals("Ngaio")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Johnsonville_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Johnsonville").getTrainServices());
		} else if (stationName.equals("Ngauranga")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Upper-Hutt_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Upper-Hutt").getTrainServices());
			UI.println(trainLines.get("Melling_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Melling").getTrainServices());
		} else if (stationName.equals("Paekakariki")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Waikanae_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Waikanae").getTrainServices());
		} else if (stationName.equals("Paraparamu")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Waikanae_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Waikanae").getTrainServices());
		} else if (stationName.equals("Paremata")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Waikanae_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Waikanae").getTrainServices());
		} else if (stationName.equals("Petone")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Upper-Hutt_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Upper-Hutt").getTrainServices());
			UI.println(trainLines.get("Melling_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Melling").getTrainServices());
			UI.println(trainLines.get("Masterton_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Masterton").getTrainServices());
		} else if (stationName.equals("Plimmerton")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Waikanae_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Waikanae").getTrainServices());
		} else if (stationName.equals("Pomare")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Upper-Hutt_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Upper-Hutt").getTrainServices());
		} else if (stationName.equals("Porirua")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Waikanae_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Waikanae").getTrainServices());
		} else if (stationName.equals("Pukerua-Bay")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Waikanae_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Waikanae").getTrainServices());
		} else if (stationName.equals("Raroa")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Johnsonville_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Johnsonville").getTrainServices());
		} else if (stationName.equals("Redwood")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Waikanae_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Waikanae").getTrainServices());
		} else if (stationName.equals("Renall-Street")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Masterton_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Masterton").getTrainServices());
		} else if (stationName.equals("Silverstream")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Upper-Hutt_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Upper-Hutt").getTrainServices());
		} else if (stationName.equals("Simla-Crescent")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Johnsonville_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Johnsonville").getTrainServices());
		} else if (stationName.equals("Solway")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Masterton_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Masterton").getTrainServices());
		} else if (stationName.equals("Taita")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Upper-Hutt_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Upper-Hutt").getTrainServices());
		} else if (stationName.equals("Takapu-Road")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Waikanae_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Waikanae").getTrainServices());
		} else if (stationName.equals("Tawa")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Waikanae_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Waikanae").getTrainServices());
		} else if (stationName.equals("Trentham")) {
			UI.println(trainLines.get("Upper-Hutt_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Upper-Hutt").getTrainServices());
		} else if (stationName.equals("Upper-Hutt")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Upper-Hutt_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Upper-Hutt").getTrainServices());
			UI.println(trainLines.get("Masterton_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Masterton").getTrainServices());
		} else if (stationName.equals("Waikanae")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Waikanae_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Waikanae").getTrainServices());
		} else if (stationName.equals("Wallaceville")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Upper-Hutt_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Upper-Hutt").getTrainServices());
		} else if (stationName.equals("Waterloo")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Upper-Hutt_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Upper-Hutt").getTrainServices());
		} else if (stationName.equals("Wellington")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Johnsonville_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Johnsonville").getTrainServices());
			UI.println(trainLines.get("Masterton_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Masterton").getTrainServices());
			UI.println(trainLines.get("Melling_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Melling").getTrainServices());
			UI.println(trainLines.get("Upper-Hutt_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Upper-Hutt").getTrainServices());
			UI.println(trainLines.get("Waikanae_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Waikanae").getTrainServices());
		} else if (stationName.equals("Western-Hutt")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Upper-Hutt_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Upper-Hutt").getTrainServices());
		} else if (stationName.equals("Wingate")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Upper-Hutt_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Upper-Hutt").getTrainServices());
		} else if (stationName.equals("Woburn")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Upper-Hutt_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Upper-Hutt").getTrainServices());
		} else if (stationName.equals("Woodside")) {
			UI.println(stationName + " has the following train lines and service times:");
			UI.println(trainLines.get("Masterton_Wellington").getTrainServices());
			UI.println(trainLines.get("Wellington_Masterton").getTrainServices());
		}
	}

	/**
	 * routePlan method allows the user to select two stations, and determine if
	 * there are train lines that connect those two stations.
	 */
	public void routePlan() {
		UI.clearText();
		Object[] stPossibleNames = { "Ava", "Awarua-Street", "Box-Hill", "Carterton", "Crofton-Downs", "Epuni",
				"Featherston", "Heretaunga", "Johnsonville", "Kenepuru", "Khandallah", "Linden", "Mana", "Manor-Park",
				"Masterton", "Matarawa", "Maymorn", "Melling", "Naenae", "Ngaio", "Ngauranga", "Paekakariki",
				"Paraparaumu", "Paremata", "Petone", "Plimmerton", "Pomare", "Porirua", "Pukerua-Bay", "Raroa",
				"Redwood", "Renal-Street", "Silverstream", "Simal-Crescent", "Solway", "Taita", "Takapu-Road", "Tawa",
				"Trentham", "Upper-Hutt", "Waikanae", "Wallaceville", "Waterloo", "Wellington", "Western-Hutt",
				"Wingate", "Woburn", "Woodside" };
		Object station1 = JOptionPane.showInputDialog(null, "Select one", "Input", JOptionPane.INFORMATION_MESSAGE,
				null, stPossibleNames, stPossibleNames[0]);
		Object station2 = JOptionPane.showInputDialog(null, "Select one", "Input", JOptionPane.INFORMATION_MESSAGE,
				null, stPossibleNames, stPossibleNames[0]);
		UI.clearText();
		if (stations.get(station1).getTrainLines().equals(stations.get(station2).getTrainLines())) {
			for (TrainLine t : stations.get(station1).getTrainLines()) {
				List<Station> stAtTL1 = new ArrayList(t.getStations());

				UI.println("Stations visited by " + t.getName() + " : " + stAtTL1);
			}
// 	commenting out code that didn't work for me
//			for (TrainLine t : stations.get(station2).getTrainLines()) {
//				List <Station> stAtTL2 = new ArrayList (t.getStations());
//				UI.println("Stations visited by " + t.getName() + " : "+ stAtTL2);
//			}
		} else
			UI.println("Unable to travel between selected stations without a train connection!");
	}

	/**
	 * loadWellyMap loads a geographical map of the Wellington region with the
	 * locations of the train lines shown.
	 */
	public void loadWellyMap() {
		UI.clearGraphics();
		String wellyMap = "geographic-map.png";
		UI.drawImage(wellyMap, 0, 0);
	}

	/**
	 * loadSystemMap displays a map of train lines and station names that the user
	 * can interact with.
	 */
	public void loadSystemMap() {
		UI.clearGraphics();
		String systemMap = "system-map.png";
		UI.drawImage(systemMap, 0, 0);
		// BufferedImage instead of drawImage so that the clickable shapes can be drawn
		// over the map.
		BufferedImage map = null;
		try {
			map = ImageIO.read(new File("system-map.png"));
		} catch (IOException e) {
		}

		// adding clicky areas
		Rectangle johnsonville = new Rectangle("Johnsonville", 60, 438, 60, 28);
		shapes.add(johnsonville);
		johnsonville.drawShape();

	}

	// attempt to print station information when you click on the outline around the
	// station name on the map
	/**
	 * doMouse defines the mouse action listener.
	 * 
	 * @param action pressed or released
	 * @param x      the X coordinate on mouse press
	 * @param y      the Y coordinate on mouse press
	 */
	public void doMouse(String action, double x, double y) {
		if (action.equalsIgnoreCase("Pressed")) {
			UI.println("In MouseListener (Pressed): " + x + ", " + y);
			this.x1 = x;
			this.y1 = y;
		} else if (action.equalsIgnoreCase("Released")) {
			this.x1 = x;
			this.y1 = y;
			for (int i = shapes.size() - 1; i >= 0; i--) {
				if (shapes.get(i).isChosen(x, y)) {
					UI.println("Selected station");
					String stationName = shapes.get(i).getName();
					UI.println(stations.get(stationName));
					UI.println(stations.get(stationName).getTrainLines());
					break;
				}
			}
		}
	}

	/**
	 * @param mosX is the X coordinate on mouse click
	 * @param mosY is the Y coordinate on mouse click
	 * @return true or false regarding whether the area clicked contains a shape and
	 *         which one if so
	 */
	public boolean isChosen(double mosX, double mosY) {
		if ((mosX > x) && (mosX < (x + w)) && (mosY > y) && (mosY < y + h)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * loadStationData class was originally designed to allow the user to load their
	 * own data file.
	 * 
	 * @deprecated
	 */
	public void loadStationData() {
		try {
			String fileName = "stations.data";
			File stationData = new File(fileName);
			Scanner sc = new Scanner(stationData);
			while (sc.hasNext()) {
				String name = sc.next();
				int zone = sc.nextInt();
				double distance = sc.nextDouble();
				sc.nextLine();
				Station s = new Station(name, zone, distance);
				stations.put(name, s);
				UI.println(s);
			}
		} catch (IOException ex) {
			UI.printf("Error loading file", ex);
		}
	}

	/**
	 * loadTrainLineData class was designed to allow user to load file before
	 * pivoting to auto-load.
	 * 
	 * @deprecated
	 */
	public void loadTrainLineData() {
		try {
			File trainLineData = new File(UIFileChooser.open());
			Scanner sc = new Scanner(trainLineData);
			while (sc.hasNext()) {
				String name = sc.nextLine();
				TrainLine t = new TrainLine(name);
				trainLines.put(name, t);
				UI.println(t);
			}
		} catch (IOException ex) {
			UI.printf("Error loading file", ex);

		}
	}

	/**
	 * main method tells the program to initialize the WellingtonTrains UI
	 * 
	 * @param args standard main construction
	 */
	public static void main(String[] args) {
		new WellingtonTrains();
	}

}
