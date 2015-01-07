package models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

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

public class Model extends Observable {

	public static Model sharedInstance = new Model();
	private Kunde kunde = new Kunde();
	SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");

	/*
	 * Konstruktor
	 */

	private Model() {
		System.out.println("Play: " + play.core.PlayVersion.current());
	}

	/*
	 * Aktuelles Datum und zeit
	 */

	public String getTime() {
		Date currentTime = new Date(System.currentTimeMillis());
		return formatter.format(currentTime);
	}

	/*
	 * Produkte in die DB inserieren
	 */

	public void produktInserieren(double preis, String artikelBezeichnung,
			String bildPfad, String kategorie, String lagermenge) {

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			conn = DB.getConnection();
			stmt = conn.createStatement();
			stmt.executeUpdate("insert into produkt values (" + preis + ","
					+ "(SELECT MAX (artikelNummer) FROM produkt)+1,'"
					+ artikelBezeichnung + "'," + " '" + bildPfad + "','"
					+ kategorie + "'," + "" + lagermenge + ");");
			System.out.println(getTime() + ":" + stmt
					+ "Produkt(e) hinzugefügt");
		}

		catch (SQLException ex) {
			ex.printStackTrace();
			System.out.println(getTime() + ": Fehler Produkt inserieren");
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

	/*
	 * In Tabelle Warenkorb Warenkorbnummer, Kundennummer, Artikelnummer und die
	 * Menge inserieren
	 */

	public void setWarenkorb(String artikelnr, String menge) {
		if (kunde != null) {

			Connection conn = null;
			Statement stmt = null;
			ResultSet rs = null;

			try {
				conn = DB.getConnection();
				stmt = conn.createStatement();
				int anzahl = stmt
						.executeUpdate("insert into Warenkorb values ("
								+ "(SELECT MAX (wkn) FROM warenkorb)+1, "
								+ kunde.getKundenNummer() + "," + artikelnr
								+ "," + menge + ");");
				System.out.println(getTime() + ": " + anzahl
						+ " Artikel in Warenkorb gelegt");
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
		} else {
			System.out.println(getTime() + ": nicht eingeloggt");
		}
		getWarenkorb();

	}

	/*
	 * Produktsuche anhand der Artikelnummer
	 */

	public Produkt artikelnummerSuchen(String ausgewaehltesProdukt) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		Produkt gesuchtesProdukt = null;
		try {
			conn = DB.getConnection();
			stmt = conn.createStatement();
			rs = stmt
					.executeQuery("SELECT * FROM produkt Where artikelNummer ="
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

	/*
	 * Methode zum
	 */

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
				rs = stmt.executeQuery("SELECT * FROM produkt;");

				break;
			case "aussen":
				rs = stmt
						.executeQuery("SELECT * FROM produkt WHERE kategorie = 'aussen' ;");

				break;
			case "innen":
				rs = stmt
						.executeQuery("SELECT * FROM produkt WHERE kategorie = 'innen' ;");

				break;
			case "brennbar":
				rs = stmt
						.executeQuery("SELECT * FROM produkt WHERE kategorie = 'brennbar' ;");

				break;
			// case "warenkorb": //eigene Methode, weil die Abfragen beim
			// Warenkorb abweichen
			// if (this.kunde != null) {
			// rs = stmt
			// .executeQuery("SELECT DISTINCT p.* FROM produkt p, Warenkorb w WHERE "
			// + "p.artikelNummer = w.artikelNummer and w.kundenNummer = '"
			// + this.kunde.getKundenNummer() + "';");
			// } else {
			// rs.close(); stmt.close(); conn.close();
			// return produkte;
			// }
			// break;
			default:
				String select = "SELECT * FROM produkt WHERE preis = ?  "
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
				// int bestellmenge = rs.getInt("bestellmenge");
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

	/*
	 * Gesuchtes Produkt
	 */

	public List<Produkt> produktSuchen(String gesuchterWert) {

		List<Produkt> gesuchteProdukte = getProdukte(gesuchterWert);

		return gesuchteProdukte;

	}

	/*
	 * Alle Produkte
	 */

	public List<Produkt> getProdukteAlle() {

		List<Produkt> produkteAlleList = getProdukte("alle");

		return produkteAlleList;
	}

	/*
	 * Produkte aus Kategorie Aussen
	 */

	public List<Produkt> getProdukteAussen() {

		List<Produkt> produkteAussen = getProdukte("aussen");

		return produkteAussen;
	}

	/*
	 * Produkte aus Kategorie Innen
	 */

	public List<Produkt> getProdukteInnen() {

		List<Produkt> produkteInnen = getProdukte("innen");

		return produkteInnen;
	}

	/*
	 * Produkte aus Kategorie Brennstoffe
	 */

	public List<Produkt> getProdukteBrennholz() {

		List<Produkt> produkteBrennstoff = getProdukte("brennbar");

		return produkteBrennstoff;
	}

	/*
	 * Produkte aus dem Warenkorb werden gezeigt/geholt
	 */

	public void getWarenkorb() {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		if (this.kunde != null) {
			try {
				conn = DB.getConnection();
				stmt = conn.createStatement();

				rs = stmt
						.executeQuery("SELECT DISTINCT p.*, w.bestellmenge FROM produkt p, Warenkorb w WHERE "
								+ "p.artikelNummer = w.artikelNummer and w.kundenNummer = '"
								+ this.kunde.getKundenNummer() + "';");
				System.out
						.println(getTime() + ": Lade Warenkorb des Kunden...");
				this.kunde.clearWarenkorb();
				while (rs.next()) {
					double preis = rs.getDouble("preis");
					String artikelNummer = rs.getString("artikelNummer");
					String artikelBezeichnung = rs
							.getString("artikelBezeichnung");
					String bildPfad = rs.getString("bildPfad");
					String kategorie = rs.getString("kategorie");
					int lagermenge = rs.getInt("lagermenge");
					int bestellmenge = rs.getInt("bestellmenge");

					this.kunde.setWarenkorb(new Produkt(preis, artikelNummer,
							artikelBezeichnung, bildPfad, kategorie,
							lagermenge, bestellmenge));

				}

				if (this.kunde.getWarenkorb().isEmpty()) {
					System.out.println(getTime() + ": Warenkorb leer...");
				} else {
					this.kunde.zeigeInhaltWarenkorb();
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

	}

	/*
	 * 
	 */

	public Kunde getKunde() {

		return kunde;
	}

	/*
	 * Logout
	 */

	public Kunde logout() {
		kunde = new Kunde();
		return getKunde();
	}

	/*
	 * Login Daten werden überprüft
	 */

	public void loginUeberpruefung(Kunde kunde) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String select = "SELECT * FROM users WHERE username = ? AND pass = ?;";
		try {
			conn = DB.getConnection();
			stmt = conn.prepareStatement(select);
			stmt.setString(1, kunde.getBenutzername());
			stmt.setString(2, verschluesselPW(kunde.getPasswort()));
			rs = stmt.executeQuery();

			if (rs.next()) {
				System.out.println(getTime() + ": pw richtig");

				boolean isAdmin = false;

				if (rs.getString("admin").equals("ja")) {
					isAdmin = true;
				}
				this.kunde = new Kunde(
						// instanziiert Kunden der aktuell eingeloggt ist
						rs.getString("kundenNummer"), rs.getString("vorname"),
						rs.getString("anrede"), rs.getString("nachname"),
						rs.getString("username"), rs.getString("email"),
						rs.getString("str"), rs.getString("hausnr"),
						rs.getString("plz"), rs.getString("ort"),
						rs.getString("telefonnr"), rs.getString("pass"),
						isAdmin);
				System.out.println(getTime() + ": Login von:\n " + this.kunde);

				this.getWarenkorb(); // Läd Warenkorb aus der DB in die
										// ArrayList des
				// Kunden
			} else {
				System.out.println(getTime() + ": Passwort nicht korrekt...");
				throw new wrongPasswordOrUsernameException();
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
			System.out.println(getTime() + ": Fehler login");
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

	/*
	 * Kunde wird im DB hinzugefügt
	 */

	public void addKunden(Kunde kunde) throws NoSuchAlgorithmException {
		Connection conn = null;
		PreparedStatement stmt = null;

		String insert = "insert into users(kundenNummer,anrede,vorname,nachname,"
				+ "username,email,str,hausnr,plz,ort,telefonnr,pass,admin)"
				+ " values((SELECT MAX (kundenNummer) FROM users)+1, "
				+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
		try {
			conn = DB.getConnection();
			stmt = conn.prepareStatement(insert);
			stmt.setString(1, kunde.getAnrede());
			stmt.setString(2, kunde.getVorname());
			stmt.setString(3, kunde.getNachname());
			stmt.setString(4, kunde.getBenutzername());
			stmt.setString(5, kunde.getEmail());	
			stmt.setString(6, kunde.getStrasse());	
			stmt.setString(7, kunde.getHausnummer());	
			stmt.setString(8, kunde.getPlz());	
			stmt.setString(9, kunde.getOrt());	
			stmt.setString(10, kunde.getTelefonnummer());	
			stmt.setString(11, verschluesselPW(kunde.getPasswort()));	
			stmt.setString(12, "nein");	
			stmt.executeUpdate();

			System.out.println(getTime() + ": " + 99
					+ " Kunde wurde hinzugefügt...");

		} catch (SQLException e) {
			System.out.println(getTime() + ": Fehler Kunden inserieren");
			e.printStackTrace();
		} finally {
			
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

	/*
	 * Autovervollständigung bei der Suche
	 */

	public String autovervollstaendigungSuche(String produkt) {
		ArrayList<String> produktbezeichnungen = new ArrayList<>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String ergebnis = "";

		try {
			conn = DB.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT artikelBezeichnung FROM produkt;");
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

	/*
	 * 
	 */

	public void setKunde(Kunde kunde) {
		this.kunde = kunde;
	}

	/*
	 * Verschlüsselung des Passworts
	 */

	public static String verschluesselPW(String pass)
			throws NoSuchAlgorithmException {

		MessageDigest md = MessageDigest.getInstance("SHA");
		md.update(pass.getBytes());
		String passwortString = "";
		for (byte b : md.digest()) {
			passwortString += Byte.toString(b);
		}

		return passwortString;
	}

	/*
	 * 
	 */

	public String getProduktJson(String artikelNummer) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		Integer menge = null;
		try {
			conn = DB.getConnection();
			stmt = conn.createStatement();
			rs = stmt
					.executeQuery("SELECT * FROM produkt WHERE artikelNummer = "
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

	/*
	 * KEINE AHNUNG
	 * :)!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1
	 */
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

	/*
	 * Ändert die Menge der vorhandenen Produkte im DB
	 */

	public static JsonNode zeigeAktuelleMenge(JsonNode obj) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		JsonNode json = obj;
		JsonNode jsonMenge = null;
		String artikelNummer = obj.get("Artikel").asText();
		Integer menge = null;

		try {
			conn = DB.getConnection();
			stmt = conn.createStatement();
			rs = stmt
					.executeQuery("SELECT * FROM produkt WHERE artikelNummer = '"
							+ artikelNummer + "' ;");

			if (rs.next()) {
				menge = new Integer(rs.getInt("lagermenge"));
			}

			ObjectMapper mapper = new ObjectMapper();
			jsonMenge = mapper.readTree("{\"Menge\":\"" + menge.toString()
					+ "\"}");

		} catch (SQLException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jsonMenge;
	}

	public void mengeAendern(String artikelnr, int menge) {

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = DB.getConnection();
			stmt = conn.createStatement();

			int anzahl = stmt
					.executeUpdate("UPDATE produkt SET lagermenge = lagermenge - "
							+ menge
							+ " WHERE artikelNummer = '"
							+ artikelnr
							+ "';");
			if (anzahl != 0) {
				System.out.println(getTime() + " ArtikelNummer " + artikelnr
						+ " bestellt...");
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

	/*
	 * 
	 */
	public void bestellArtikelAusWarenkorb() {

		for (Produkt produkt : this.kunde.getWarenkorb()) {
			mengeAendern(produkt.artikelNummer, produkt.bestellmenge);
		}
		this.kunde.clearWarenkorb();
		warenkorbDatenbankLeeren();
		getWarenkorb();

	}

	/*
	 * Löscht Produkte aus der Warenkorb anhand der Kundennumer
	 */

	public void warenkorbDatenbankLeeren() {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			conn = DB.getConnection();
			stmt = conn.createStatement();

			int anzahl = stmt
					.executeUpdate("DELETE FROM Warenkorb WHERE kundenNummer = '"
							+ this.kunde.getKundenNummer() + "';");
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
