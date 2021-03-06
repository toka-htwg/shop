package models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import models.*;
import play.db.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Model extends Observable{

	public static Model sharedInstance = new Model();
	private Kunde kunde = new Kunde();
	private HashMap<String,Kunde> kunden = new HashMap<>();
	SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
	Kunde kundeGast = new Kunde();

	private Model() {
		System.out.println("Play: " + play.core.PlayVersion.current());
		inizializeDatabase();
		kunden.put(kundeGast.kundenNummer, kundeGast);
	}
	
	public void removeCustomerFromMap(String kundenNummer){
		kunden.remove(kundenNummer);
		System.out.println(getTime() + ": removed from HashMap: "+kundenNummer);
	}
	
	public String getCustomerName(String kundenNummer){
		String vorname = "guest";
		if(kunden.containsKey(kundenNummer)){
			vorname = kunden.get(kundenNummer).vorname;
		}
		return vorname;
	}

	public String getTime() {
		Date currentTime = new Date(System.currentTimeMillis());
		return formatter.format(currentTime);
	}

//	public void produktInserieren(double preis, String artikelBezeichnung,
//			String bildPfad, String kategorie, String lagermenge) {
//
//		Connection conn = null;
//		Statement stmt = null;
//		ResultSet rs = null;
//
//		try {
//			conn = DB.getConnection();
//			stmt = conn.createStatement();
//			stmt.executeUpdate("insert into Produkt values (" + preis + ","
//					+ "(SELECT MAX (artikelNummer) FROM Produkt)+1,'"
//					+ artikelBezeichnung + "'," + " '" + bildPfad + "','"
//					+ kategorie + "'," + "" + lagermenge + ");");
//			System.out.println(getTime() + ":" + stmt
//					+ "Produkt(e) hinzugefügt");
//		}
//
//		catch (SQLException ex) {
//			ex.printStackTrace();
//			System.out.println(getTime() + ": Fehler Produkt inserieren");
//		} finally {
//			if (rs != null) {
//				try {
//					rs.close();
//				} catch (SQLException e) {
//				}
//			}
//			if (stmt != null) {
//				try {
//					stmt.close();
//				} catch (SQLException e) {
//				}
//			}
//			if (conn != null) {
//				try {
//					conn.close();
//				} catch (SQLException e) {
//				}
//			}
//		}
//	}
	
	public boolean isWarenkorbEmpty(){
		boolean isEmpty = false;
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = DB.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM warenkorb");
			if(!rs.next()){
				isEmpty = true;
			}
		} catch (SQLException e) {
			
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
		
	
		
		return isEmpty;
	}
	
	public void setWarenkorb(String artikelnr, String menge, String kundenNummer) {
		

			Connection conn = null;
			Statement stmt = null;
			ResultSet rs = null;
			

			try {
				conn = DB.getConnection();
				stmt = conn.createStatement();
				int anzahl = 0;
				rs = stmt.executeQuery("SELECT * FROM warenkorb WHERE "
						+ "kundenNummer ="+kundenNummer+" AND artikelNummer = "+artikelnr+" ;");
				if(rs.next()){
					String wkn = rs.getString("wkn");
					stmt.executeUpdate("UPDATE warenkorb "
							+ "SET bestellmenge = bestellmenge + "+menge+" WHERE wkn = "+wkn+";");
					System.out.println("Menge Artikel Warenkorb geändert");
				}
				else{
					if(isWarenkorbEmpty()){
						
						anzahl = stmt
								.executeUpdate("insert into Warenkorb values ("
										+ "1, "
										+ kundenNummer + "," + artikelnr
										+ "," + menge + ");");
					} else {
						anzahl = stmt
								.executeUpdate("insert into Warenkorb values ("
										+ "(SELECT MAX (wkn) FROM warenkorb)+1, "
										+ kundenNummer + "," + artikelnr
										+ "," + menge + ");");
					}
					
					System.out.println(getTime() + ": " + anzahl
							+ " Artikel in Warenkorb gelegt");
				}
				
			} catch (SQLException ex) {
				ex.printStackTrace();
				System.out.println(getTime() + ": Fehler Warenkorb inserieren");
			} finally {
				if (rs != null) {
					try {
						rs.close();
					} catch (SQLException e) {
					}
				}
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
					}
				}
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException e) {
					}
				}
			}
		
		

	}

	public Produkt artikelnummerSuchen(String ausgewaehltesProdukt) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		Produkt gesuchtesProdukt = null;
		try {
			conn = DB.getConnection();
			stmt = conn.createStatement();
			rs = stmt
					.executeQuery("SELECT * FROM Produkt Where artikelNummer ="
							+ ausgewaehltesProdukt + ";");
			if (rs.next()) {
				double preis = rs.getDouble("preis");
				String artikelNummer = rs.getString("artikelNummer");
				String artikelBezeichnung = rs.getString("artikelBezeichnung");
				String bildPfad = rs.getString("bildPfad");
				String kategorie = rs.getString("kategorie");
				int lagermenge = rs.getInt("lagermenge");
				System.out.println(getTime()
						+ ": "
						+ new Produkt(preis, artikelNummer, artikelBezeichnung,
								bildPfad, kategorie, lagermenge));
				gesuchtesProdukt = new Produkt(preis, artikelNummer,
						artikelBezeichnung, bildPfad, kategorie, lagermenge);
			}

		} catch (SQLException ex) {
			ex.printStackTrace();
			System.out.println(getTime() + ": Fehler Produkt suchen");
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}

		ausgewaehltesProdukt = null;
		return gesuchtesProdukt;
	}

	public List<Produkt> getProdukte(String wo) {
		List<Produkt> produkte = new ArrayList<Produkt>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		PreparedStatement prep = null;
		try {
			conn = DB.getConnection();
			stmt = conn.createStatement();

			switch (wo) {

			case "alle":
				rs = stmt.executeQuery("SELECT * FROM Produkt;");
				break;
			case "aussen":
				rs = stmt
						.executeQuery("SELECT * FROM Produkt WHERE kategorie = 'aussen' ;");
				break;
			case "innen":
				rs = stmt
						.executeQuery("SELECT * FROM Produkt WHERE kategorie = 'innen' ;");
				break;
			case "brennbar":
				rs = stmt
						.executeQuery("SELECT * FROM Produkt WHERE kategorie = 'brennbar' ;");
				break;
			default:
				String select = "SELECT * FROM Produkt WHERE preis = ?  "
						+ "OR artikelBezeichnung = ? OR kategorie = ?;";
				prep = conn.prepareStatement(select);
				prep.setString(1, wo);
				prep.setString(2, wo);
				prep.setString(3, wo);
				rs = prep.executeQuery();

				break;
			}

			while (rs.next()) {
				double preis = rs.getDouble("preis");
				String artikelNummer = rs.getString("artikelNummer");
				String artikelBezeichnung = rs.getString("artikelBezeichnung");
				String bildPfad = rs.getString("bildPfad");
				String kategorie = rs.getString("kategorie");
				int lagermenge = rs.getInt("lagermenge");
				produkte.add(new Produkt(preis, artikelNummer,
						artikelBezeichnung, bildPfad, kategorie, lagermenge));
			}

		} catch (SQLException e) {
			System.out.println(getTime() + ": Fehler Produkt suchen");
			e.printStackTrace();
		} finally {
			
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}

		return produkte;
	}

	public List<Produkt> produktSuchen(String gesuchterWert) {

		List<Produkt> gesuchteProdukte = getProdukte(gesuchterWert);

		return gesuchteProdukte;

	}

	public List<Produkt> getProdukteAlle() {

		List<Produkt> produkteAlleList = getProdukte("alle");

		return produkteAlleList;
	}

	public List<Produkt> getProdukteAussen() {

		List<Produkt> produkteAussen = getProdukte("aussen");

		return produkteAussen;
	}

	public List<Produkt> getProdukteInnen() {

		List<Produkt> produkteInnen = getProdukte("innen");

		return produkteInnen;
	}

	public List<Produkt> getProdukteBrennholz() {

		List<Produkt> produkteBrennstoff = getProdukte("brennbar");

		return produkteBrennstoff;
	}

	public List<Produkt> getWarenkorb(String kundenNummer) {
		List<Produkt> produkte = new ArrayList<Produkt>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		if (this.kunde != null) {
			try {
				conn = DB.getConnection();
				stmt = conn.createStatement();

			
				
					rs = stmt
							.executeQuery("SELECT DISTINCT p.*, w.bestellmenge FROM Produkt p, Warenkorb w WHERE "
									+ "p.artikelNummer = w.artikelNummer and w.kundenNummer = '"
									+ kundenNummer + "';");
					System.out.println(getTime()+": Lade Warenkorb des Kunden...");
					
					while (rs.next()) {
						double preis = rs.getDouble("preis");
						String artikelNummer = rs.getString("artikelNummer");
						String artikelBezeichnung = rs
								.getString("artikelBezeichnung");
						String bildPfad = rs.getString("bildPfad");
						String kategorie = rs.getString("kategorie");
						int lagermenge = rs.getInt("lagermenge");
						int bestellmenge = rs.getInt("bestellmenge");
	
						produkte.add(new Produkt(preis, artikelNummer,
								artikelBezeichnung, bildPfad, kategorie,
								lagermenge, bestellmenge));
	
					}

			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (rs != null) {
					try {
						rs.close();
					} catch (SQLException e) {
					}
				}
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
					}
				}
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException e) {
					}
				}
			}
		} else {

			System.out.println(getTime() + ": Kunde nicht eingeloggt");
		}
		
		return produkte;

	}

	public Kunde getKunde() {

		return kunde;
	}

	public Kunde logout() {
		kunde = new Kunde();
		return getKunde();
	}

	public Kunde loginUeberpruefung(Kunde kunde) {

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		Kunde kundeLoggedIn = null;

		String select = "SELECT * FROM Kunde WHERE benutzername = ? AND passwort = ?;";
		try {
			conn = DB.getConnection();
			stmt = conn.prepareStatement(select);
			stmt.setString(1, kunde.getBenutzername());
			stmt.setString(2, verschluesselPW(kunde.getPasswort()));
			rs = stmt.executeQuery();

			if (rs.next()) {
				System.out.println(getTime() + ": pw richtig");

				boolean isAdmin = false;

				if (rs.getString("isAdmin").equals("ja")) {
					isAdmin = true;
				}
				kundeLoggedIn = new Kunde(
						// instanziiert Kunden der aktuell eingeloggt ist
						rs.getString("kundenNummer"), rs.getString("vorname"),
						rs.getString("anrede"), rs.getString("nachname"),
						rs.getString("benutzername"), rs.getString("email"),
						rs.getString("strasse"), rs.getString("hausnummer"),
						rs.getString("plz"), rs.getString("ort"),
						rs.getString("telefon"), rs.getString("passwort"),
						isAdmin);
				System.out.println(getTime() + ": Login von " + kundeLoggedIn.vorname+" "+kundeLoggedIn.nachname);
				kunden.put(kundeLoggedIn.kundenNummer, kundeLoggedIn);
				System.out.println(getTime() + ": Added to HashMap: "+kundeLoggedIn.kundenNummer);
				
				// Kunden
			} else {
				System.out.println(getTime() + ": passwort nicht korrekt...");
				
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
			System.out.println(getTime() + ": Fehler login");
		} finally {
			if (rs != null) {try {rs.close();} catch (SQLException e) {}}
			if (stmt != null) {try {stmt.close();} catch (SQLException e) {}}
			if (conn != null) {try {conn.close();} catch (SQLException e) {}}
		}
		return kundeLoggedIn;
	}

	public boolean addCustomer(Kunde kunde) throws NoSuchAlgorithmException {
		Connection conn = null;
		Statement stmtCheckKundeIsEmpty = null;
		PreparedStatement stmtAddKunde = null;
		ResultSet rs = null;
		String insert="";
		boolean kundeAngelegt = false;
		
		
		
		
		if(checkForValidRegistration(kunde)){		//Regex für Einträge in Registrierung
	
			try {
				conn = DB.getConnection();
				stmtCheckKundeIsEmpty = conn.createStatement();
				rs = stmtCheckKundeIsEmpty.executeQuery("SELECT * FROM Kunde");
				if(!rs.next()){		//noch keine Kunden
					 insert = "insert into Kunde(kundenNummer,anrede,vorname,nachname,"
							+ "benutzername,email,strasse,hausnummer,plz,ort,telefon,passwort,isAdmin)"
							+ " values(1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
				} else {			
					 insert = "insert into Kunde(kundenNummer,anrede,vorname,nachname,"
							+ "benutzername,email,strasse,hausnummer,plz,ort,telefon,passwort,isAdmin)"
							+ " values((SELECT MAX (kundenNummer) FROM Kunde)+1, "
							+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
				}
				stmtAddKunde = conn.prepareStatement(insert);
				stmtAddKunde.setString(1, kunde.getAnrede());
				stmtAddKunde.setString(2, kunde.getVorname());
				stmtAddKunde.setString(3, kunde.getNachname());
				stmtAddKunde.setString(4, kunde.getBenutzername());
				stmtAddKunde.setString(5, kunde.getEmail());	
				stmtAddKunde.setString(6, kunde.getStrasse());	
				stmtAddKunde.setString(7, kunde.getHausnummer());	
				stmtAddKunde.setString(8, kunde.getPlz());	
				stmtAddKunde.setString(9, kunde.getOrt());	
				stmtAddKunde.setString(10, kunde.getTelefon());	
				stmtAddKunde.setString(11, verschluesselPW(kunde.getPasswort()));	
				stmtAddKunde.setString(12, "nein");	
				stmtAddKunde.executeUpdate();
			
				kundeAngelegt = true;
				
			} catch (SQLException e) {
				System.out.println(getTime() + ": Fehler Kunden inserieren");
				e.printStackTrace();
			} finally {
				
				if (stmtAddKunde != null) {
					try {
						stmtAddKunde.close();
					} catch (SQLException e) {
					}
				}
				if (stmtCheckKundeIsEmpty != null) {
					try {
						stmtCheckKundeIsEmpty.close();
					} catch (SQLException e) {
					}
				}
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException e) {
					}
				}
			}
		} 
		
		return kundeAngelegt;
	}
	
	/*
	 * Überprüfung der Einträge im Registrierungsformular. Durchführung im Code und nicht in Java-
	 * skript, da JS deaktiviert werden kann...
	 */
	
	public boolean checkForValidRegistration(Kunde kunde){
		boolean registrationIsValid = true;
		
		String regexEmail = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
		String regexStrasse = "\\D";
		
		
		if(!kunde.email.matches(regexEmail)){
			registrationIsValid = false;
		}
		if(!kunde.strasse.matches(regexStrasse)){   //keine Hausnummer im Strassennamen
			registrationIsValid = false;
		}
		
		return registrationIsValid;
	}
	

	public String autovervollstaendigungSuche(String produkt) {
		ArrayList<String> produktbezeichnungen = new ArrayList<>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String ergebnis = "";

		try {
			conn = DB.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT artikelBezeichnung FROM Produkt;");
			while (rs.next()) {
				produktbezeichnungen.add(rs.getString("artikelBezeichnung"));
			}
			boolean sorted = false;
			String[] meinTextArray = produktbezeichnungen
					.toArray(new String[produktbezeichnungen.size()]);
			String eingabe = produkt;
			if (null != eingabe && 0 < eingabe.trim().length()) {
				if (!sorted) {
					java.util.Arrays.sort(meinTextArray);
					sorted = true;
				}
				StringBuffer auswahl = new StringBuffer();
				boolean resultFound = false;
				for (int i = 0; i < meinTextArray.length; i++) {
					if (meinTextArray[i].toUpperCase().startsWith(
							eingabe.toUpperCase())) {
						auswahl.append(meinTextArray[i]).append(";");
						resultFound = true;
					} else {
						if (resultFound)
							break;
					}
				}
				if (0 < auswahl.length()) {
					auswahl.setLength(auswahl.length() - 1);
				}
				System.out.println(auswahl.toString());
				ergebnis = auswahl.toString();
			}

		} catch (SQLException e) {
			System.out.println(getTime()
					+ ": Fehler beim Auslesen der Artikelbezeichnung");
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
		return ergebnis;
	}

	public void setKunde(Kunde kunde) {
		this.kunde = kunde;
	}

	public static String verschluesselPW(String passwort) {
		String passwortString = "";
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA");
			md.update(passwort.getBytes());
			
			for (byte b : md.digest()) {
				passwortString += Byte.toString(b);
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return passwortString;
	}

	public String getProduktJson(String artikelNummer) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		Integer menge = null;
		try {
			conn = DB.getConnection();
			stmt = conn.createStatement();
			rs = stmt
					.executeQuery("SELECT * FROM Produkt WHERE artikelNummer = "
							+ artikelNummer + ";");

			if (rs.next()) {
				menge = new Integer(rs.getInt("lagermenge"));
			}

		} catch (SQLException ex) {
			ex.printStackTrace();
			System.out.println(getTime() + ": Fehler Produkt suchen");
		} catch (Exception e) {

			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}

		return menge.toString();
	}

	public static JSONArray convertToJson(ResultSet resultSet) throws Exception {
		JSONArray jsonArray = new JSONArray();
		while (resultSet.next()) {
			int total_rows = resultSet.getMetaData().getColumnCount();
			JSONObject obj = new JSONObject();
			for (int i = 0; i < total_rows; i++) {
				obj.put(resultSet.getMetaData().getColumnLabel(i + 1)
						.toLowerCase(), resultSet.getObject(i + 1));
			}
			jsonArray.put(obj);
		}
		return jsonArray;

	}

	public static JsonNode zeigeAktuelleMenge(JsonNode obj) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		
		JsonNode jsonMenge = null;
		String artikelNummer = obj.get("Artikelnummer").asText();
		Integer menge = null;

		try {
			conn = DB.getConnection();
			stmt = conn.createStatement();
			rs = stmt
					.executeQuery("SELECT * FROM Produkt WHERE artikelNummer = '"
							+ artikelNummer + "' ;");

			if (rs.next()) {
				menge = new Integer(rs.getInt("lagermenge"));
			}

			ObjectMapper mapper = new ObjectMapper();
			jsonMenge = mapper.readTree("{\"Artikelnummer\":\""+artikelNummer+"\",\"Menge\":\"" + menge.toString()	+ "\"}");
			System.out.println("JSON-Menge: "+jsonMenge);

		} catch (SQLException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}

		return jsonMenge;
	}

	public void mengeAendern(String artikelNummer, int menge) {

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = DB.getConnection();
			stmt = conn.createStatement();

			int anzahl = stmt
					.executeUpdate("UPDATE Produkt SET lagermenge = lagermenge - "
							+ menge
							+ " WHERE artikelNummer = '"
							+ artikelNummer
							+ "';");
			if (anzahl != 0) {
				System.out.println(getTime() + " ArtikelNummer " + artikelNummer
						+ " bestellt...");
				if(countObservers()>0){
					setChanged();
					notifyObservers(artikelNummer);
				}
				
			}
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println(getTime() + ": Fehler beim aendern der Menge");
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	public void bestellArtikelAusWarenkorb(String kundenNummer) {

		for (Produkt produkt : getWarenkorb(kundenNummer)) {
			mengeAendern(produkt.artikelNummer, produkt.bestellmenge);
		}
		
		warenkorbDatenbankLeeren(kundenNummer);
	}

	public void warenkorbDatenbankLeeren(String kundenNummer) {

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			conn = DB.getConnection();
			stmt = conn.createStatement();

			int anzahl = stmt
					.executeUpdate("DELETE FROM Warenkorb WHERE kundenNummer = '"+ kundenNummer + "';");
			
			if (anzahl == 1) {
				System.out.println(getTime() + ": " + anzahl
						+ " Eintrag aus Warenkorb geloescht");

			} else {
				System.out.println(getTime() + ": " + anzahl
						+ " Eintraege aus Warenkorb geloescht");
			}
		} catch (SQLException e) {
			System.out.println(getTime() + ": Fehler beim aendern der Menge");
			e.printStackTrace();
		} finally {
			if (rs != null) {try {rs.close();} catch (SQLException e) {}}
			if (stmt != null) {try {stmt.close();} catch (SQLException e) {}}
			if (conn != null) {try {conn.close();} catch (SQLException e) {}}
		}
	}
	
	public void inizializeDatabase(){
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = DB.getConnection();
			DatabaseMetaData meta = conn.getMetaData();
			stmt = conn.createStatement();
			rs = meta.getTables(null, null, "Produkt", null);
			if(!rs.next()){							//prüft ob Tabelle "Produkt" bereits initialisiert wurde
			stmt.executeUpdate(
				"CREATE TABLE Produkt ("
				+ "`preis` double NOT NULL,	"
				+ "`artikelNummer`	INTEGER NOT NULL,"
				+ "`artikelBezeichnung`	varchar(50) NOT NULL,"
				+ "`bildPfad`	varchar(20),`kategorie`	varchar(20) NOT NULL,"
				+ "`lagermenge`	INTEGER,PRIMARY KEY(artikelNummer));"
				+ "INSERT INTO `Produkt` VALUES('99.99',1,'Gartenzaun','images/Palissaden.jpg','aussen',90);"
				+ "INSERT INTO `Produkt` VALUES('119.99',2,'Palisaden fuer den Garten','images/Pfaehle.jpg','aussen',98);"
				+ "INSERT INTO `Produkt` VALUES('249.99',3,'Terassenbelaege','images/Terrasse.jpg','aussen',7);"
				+ "INSERT INTO `Produkt` VALUES('49.99',4,'Terassenmoebel','images/bruecke.jpg','aussen',9);"
				+ "INSERT INTO `Produkt` VALUES('49.99',5,'Esstisch','images/esstisch.jpg','innen',60);"
				+ "INSERT INTO `Produkt` VALUES('4.99',6,'Pellets','images/Pellets.jpg','brennbar',79);"
				+ "INSERT INTO `Produkt` VALUES('22.99',7,'Holzfahrrad','images/Holzfahrrad.jpg','aussen',9);"
				+ "INSERT INTO `Produkt` VALUES('19.99',12,'Stuhl','images/stuhl.jpg','innen',80);"
				+ "INSERT INTO `Produkt` VALUES('44.99',13,'Vertäfelung','images/Vertaefelung.jpg','innen',90);"
				+ "INSERT INTO `Produkt` VALUES('4.99',14,'Echtes Kiefernholz','images/Kiefernholz.jpg','brennbar',4);"
				+ "INSERT INTO `Produkt` VALUES('5.99',15,'Echtes Buchenholz','images/Buchenholz.jpg','brennbar',5);"
				+ ";");
			}
			rs.close();
			rs = meta.getTables(null, null, "Warenkorb", null);
			if(!rs.next()){							//prüft ob Tabelle "Warenkorb" bereits initialisiert wurde
			stmt.executeUpdate(
				"CREATE TABLE Warenkorb ("
						+ "	`wkn`	INTEGER NOT NULL,"
						+ "	`kundenNummer`	INTEGER NOT NULL,"
						+ "	`artikelNummer`	INTEGER NOT NULL,"
						+ "	`bestellmenge`	INTEGER NOT NULL,"
						+ "	PRIMARY KEY(wkn)"
						+ ");"
				+ ";");
			}
			rs.close();
			rs = meta.getTables(null, null, "Kunde", null);
			if(!rs.next()){							//prüft ob Tabelle "Kunde" bereits initialisiert wurde
			stmt.executeUpdate(
					"CREATE TABLE Kunde ("
					+ "	`kundenNummer`	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
					+ "	`anrede`	TEXT,"
					+ "	`vorname`	varchar(20) NOT NULL,"
					+ "	`nachname`	varchar(20) NOT NULL,"
					+ "	`benutzername`	TEXT NOT NULL UNIQUE,"
					+ "	`email`	TEXT,"
					+ "	`strasse`	TEXT NOT NULL,"
					+ "	`hausnummer`	TEXT NOT NULL,"
					+ "	`plz`	TEXT NOT NULL,"
					+ "	`ort`	TEXT NOT NULL,"
					+ "	`telefon`	TEXT,"
					+ "	`passwort`	varchar(10) NOT NULL,"
					+ "	`isAdmin`	TEXT);"
					+ "INSERT INTO `Kunde` VALUES(1000,'Herr','Dumitru','Mihu','Dumitru','ist@bekannt.de','Brauneggerstrasse',55,78467,'Konstanz','','-8774-113-27-52-79-101-9028768115-45-111-23-121-10447-69-45','ja');"
					+ "INSERT INTO `Kunde` VALUES(1001,'Herr','Georg','Mohr','Georg','ist@bekannt.de','Brauneggerstrasse',55,'78467','Konstanz','','-8774-113-27-52-79-101-9028768115-45-111-23-121-10447-69-45','ja');"
					+ "INSERT INTO `Kunde` VALUES(1002,'Herr','Sebastian','Thuemmel','Basti','ist@bekannt.de','Brauneggerstrasse',55,78467,'Konstanz','','-8774-113-27-52-79-101-9028768115-45-111-23-121-10447-69-45','ja');"
					+ "INSERT INTO `Kunde` VALUES(1003,'Herr','Max','Mustermann','Maxi','Max@Mustermann.de','Mustermannstrasse',1337,78467,'Musterstadt','0190/666666','-8774-113-27-52-79-101-9028768115-45-111-23-121-10447-69-45','nein');"
					+ "INSERT INTO `Kunde` VALUES(1004,'Herr','Bernd','Brot','broti','ich@du.de','Pupsweg',13,78467,'Konstanz',0190,'-8774-113-27-52-79-101-9028768115-45-111-23-121-10447-69-45','nein');"
					+ "INSERT INTO `Kunde` VALUES(1005,'Herr','Jim','Levenstein','Kuchenficker','hab@ich.de','weiß','ich','nicht','stadt',0191,'-8774-113-27-52-79-101-9028768115-45-111-23-121-10447-69-45','nein');"
					+ "INSERT INTO `Kunde` VALUES(1006,'Frau','Gina','Wilde','Gina','asd@aa.de','Funstr','22a',74689,'KN',92381,'-8774-113-27-52-79-101-9028768115-45-111-23-121-10447-69-45','nein');"
					+ "INSERT INTO `Kunde` VALUES(1007,'Frau','Anna','huh','AM','gsdad@asas.de','sadas',12,2112,'asasd','','-376-17122789612731-52-11010-4250-102109-14-33-103-92-24','nein');"
					+ "INSERT INTO `Kunde` VALUES(1008,'F','Agnes','Klein','Agi','Agnes@Klein.de','hier',17,78467,'Konstanz','','-8774-113-27-52-79-101-9028768115-45-111-23-121-10447-69-45','nein');"
					+ "INSERT INTO `Kunde` VALUES(1009,'H','depp','vom','depp','gibts@nicht.de','dienst',1337,12,'hier',01901,'-8774-113-27-52-79-101-9028768115-45-111-23-121-10447-69-45','nein');"
					+ "INSERT INTO `Kunde` VALUES(1010,'Herr','sdf','sdf','deppi','sdf','sdf','sdf','sdf','sdf','sdfsdf','-8774-113-27-52-79-101-9028768115-45-111-23-121-10447-69-45','nein');"
					+ "INSERT INTO `Kunde` VALUES(1011,'Frau','Agnes','Klein','Ackness','senga1@gmx.net','Buhlenweg 38','Buhlenweg 38',78467,'Konstanz','+4915115331366','113-60293-39-119123547962-69-76447279-1261012128','nein');"
					+ ";");
			}
			rs.close();
			rs = meta.getTables(null, null, "Bestellung", null);
			if(!rs.next()){							//prüft ob Tabelle "Bestellung" bereits initialisiert wurde
			stmt.executeUpdate(
					"CREATE TABLE Bestellung ("
						+ "	`kundenNummer`	INTEGER NOT NULL,"
						+ "	`artikelNummer`	INTEGER NOT NULL,"
						+ "	PRIMARY KEY(kundenNummer,artikelNummer)"
						+ ");"
						+ ";");
							
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}

			
	}
	

}
