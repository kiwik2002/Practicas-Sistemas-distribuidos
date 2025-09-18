package ficheros_prac1.ficheros_prac1.ReservasLocal.src.reservas;

import org.json.simple.JSONArray;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;


public class GestorReservas {

	private static final Vector<Sesion> NULL = null;

	private FileWriter os;			// stream para escribir los datos de las reservas en el fichero

	// Sesiones de la próxima semana indexadas por el día de la semana. Empiezan mañana, cuando se puede reservar
	final private HashMap<DiaSemana, Vector<Sesion>> sesionesSemana;
	// Reservas indexadas por el código de usuario
	
	
	final private HashMap<String, Vector<Reserva>> reservas;

	/**
	 * Constructor del gestor de reservas
	 * Crea o lee el fichero con datos de prueba por defecto, dependiendo de que exista el fichero
	 */
	public GestorReservas() {

		this.sesionesSemana = new HashMap<DiaSemana, Vector<Sesion>>();
		this.reservas = new HashMap<String, Vector<Reserva>>();

		// Genera las sesiones de la próxima semana con todas las plazas libres inicialmente
		generaSesiones();

		File file = new File("reservas.json");
		try {
			if (!file.exists() ) {
				// Si no existen el fichero de datos, los genera con valores por defecto.
				// Hace las correspondientes reservas modificando los diccionarios de sesiones y reservas
				os = new FileWriter(file);
				generaReservas();
				escribeFichero(os);
				os.close();
			}
			else {
				// Si existe el fichero, lo lee, y hace las correspondientes reservas
				FileReader is = new FileReader(file);
				leeFichero(is);
				is.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Genera y almacena las sesiones disponibles para cada día de la próxima semana.
	 * 
	 * Se asignan diferentes actividades a cada día con horarios y plazas predefinidas.
	 * Las sesiones se almacenan en un diccionario indexado por `DiaSemana`.
	 */
	private void generaSesiones() {
		for (DiaSemana diaSemana : DiaSemana.values()) {
			Vector<Sesion> sesionesDia = new Vector<>(); // Asegura que nunca sea null

			switch (diaSemana) {
			case lunes -> {
				sesionesDia.add(new Sesion("Taichí", 9, 3));
				sesionesDia.add(new Sesion("Ironfit", 10, 3));
			}
			case martes -> {
				sesionesDia.add(new Sesion("Ironfit", 10, 3));
				sesionesDia.add(new Sesion("Yoga", 18, 3));
			}
			case miercoles -> {
				sesionesDia.add(new Sesion("Taichí", 9, 3));
				sesionesDia.add(new Sesion("Yoga", 18, 3));
			}
			case jueves -> {
				sesionesDia.add(new Sesion("Taichí", 9, 3));
				sesionesDia.add(new Sesion("Ironfit", 10, 3));
			}
			case viernes -> {
				sesionesDia.add(new Sesion("Ironfit", 10, 3));
				sesionesDia.add(new Sesion("Yoga", 18, 3));
			}
			default -> {} // No se definen sesiones para sábado y domingo
			}

			// Solo guarda los posibles sesiones del día
			if (!sesionesDia.isEmpty()) {
				sesionesSemana.put(diaSemana, sesionesDia);
			}
		}
	}

	/**
	 * Genera los datos iniciales y los guarda en los diccionarios
	 */
	private void generaReservas() {

		hazReserva("cli01", "Taichí", DiaSemana.lunes, 9);     
		hazReserva("cli01", "Taichí", DiaSemana.miercoles, 9);  
		hazReserva("cli02", "Taichí", DiaSemana.lunes, 9);     
		hazReserva("cli03", "Ironfit", DiaSemana.martes, 10); 
		hazReserva("cli04", "Ironfit", DiaSemana.martes, 10); 
		hazReserva("cli05", "Ironfit", DiaSemana.martes, 10); 
		hazReserva("cli05", "Yoga", DiaSemana.martes, 18);    
		hazReserva("cli05", "Yoga", DiaSemana.miercoles, 18);    
		hazReserva("cli06", "Ironfit", DiaSemana.jueves, 10); 
		hazReserva("cli07", "Ironfit", DiaSemana.jueves, 10); 
	}



	/**
	 * Cuando cada usuario cierra su sesión volcamos los datos en el fichero para mantenerlo actualizado
	 */
	public void guardaDatos(){
		File file = new File("reservas.json");
		try {
			os = new FileWriter(file);
			escribeFichero(os);
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Escribe en el fichero un array JSON con los datos de las reservas guardadas en el diccionario
	 *
	 * @param os	stream de escritura asociado al fichero de datos
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	private void escribeFichero(FileWriter os) throws IOException {
		// POR IMPLEMENTAR
		JSONArray arrayJSONUsuarios = new JSONArray() ;
		Set<String> keys = reservas.keySet();
		for(String key : keys) {
			JSONArray arrayJSONReservas = new JSONArray() ;
			Vector<Reserva> reservasUser = reservas.get(key);
			for(Reserva reserva :reservasUser) {
				JSONObject objetoJSON = reserva.toJSON();
				arrayJSONReservas.add(objetoJSON); 
			}
			JSONObject usuarioJSON = new JSONObject();
			usuarioJSON.put("codigo", key);
	        usuarioJSON.put("reservas", arrayJSONReservas);
			arrayJSONUsuarios.add(usuarioJSON);
		}
		os.write(arrayJSONUsuarios.toJSONString());
		os.flush();
		
		
	}



	/**
	 * Lee los datos almacenados de un archivo JSON y los carga en el diccionario en memoria.
	 *
	 * @param is Stream de lectura del archivo que contiene los datos en formato JSON.
	 */
	private void leeFichero(FileReader is) {
		JSONParser parser = new JSONParser();

		try {
			// Leemos toda la información del fichero en un array de objetos JSON
			JSONArray array = (JSONArray) parser.parse(is);

			// Si hay sesiones, las incluimos en el diccionario
			if (array != null && !array.isEmpty()) {
				rellenaDiccionarios(array);
			} else {
				System.err.println("Advertencia: El archivo JSON está vacío o no contiene datos válidos.");
			}
		} catch (IOException e) {
			System.err.println("Error al leer el archivo JSON: " + e.getMessage());
			e.printStackTrace();
		} catch (ParseException e) {
			System.err.println("Error al parsear el archivo JSON: " + e.getMessage());
			e.printStackTrace();
		}
	}
	

	/**
	 * Rellena y modifica los diccionarios a partir de los datos en un JSONArray
	 *
	 * @param array	JSONArray con los datos de los paquetes
	 */
	private void rellenaDiccionarios(JSONArray array) {
		Iterator<JSONObject> iterJsonObject = array.iterator();
		while(iterJsonObject.hasNext()) {
			JSONObject info = iterJsonObject.next();
			//aqui tienes que extraer info del Usuario
			String codigoCliente = info.get("codigo").toString();
			JSONArray infoReservasUsuario = (JSONArray) info.get("reservas"); 
			Iterator<JSONObject> iterReservas = infoReservasUsuario.iterator();
			while(iterReservas.hasNext()) {
				JSONObject inforeserva = iterReservas.next();
				Reserva reserva = new Reserva(inforeserva);
				indexarReserva(reserva);
					
			}
		
		}
        
	}



	/**
	 * Busca y devuelve una sesión determinada en función de la actividad, el día y la hora especificados.
	 *
	 * @param actividad Nombre de la actividad a buscar.
	 * @param dia Día de la semana en el que se realiza la sesión.
	 * @param hora Hora de la sesión en formato 24 horas.
	 * @return La sesión encontrada o `null` si no existe una sesión con esos parámetros.
	 */
	Sesion buscaSesion(String actividad, DiaSemana dia, long hora) {
        Vector<Sesion> sesionesDia = sesionesSemana.get(dia);
        if(sesionesDia != null) {

        	for(Sesion  clase : sesionesDia){
        		if(clase.getActividad().equals(actividad) && clase.getHora() == hora ) {
        			return clase;
        		}
        	}
        }
        return null; 
	}


	/**
	 * Obtiene una lista de todas las reservas del usuario específico.
	 *
	 * @param codUsuario El código del usuario cuyas reservas se desea listar
	 * @return Un `JSONArray` que contiene la representación JSON de cada reserva del usuario.
	 */
	//duda que hago si el usuario no existe 
	@SuppressWarnings("unchecked")
	public JSONArray listaReservasUsuario(String codUsuario) {
        // POR IMPLEMENTAR
		JSONArray array = new JSONArray();
		Vector<Reserva> reservasUsuario = reservas.get(codUsuario);
		if(reservasUsuario != null) {
			for(Reserva reserva : reservasUsuario) {
				array.add(reserva.toJSON());
			}
			return array ;
		}
        return array; // MODIFICAR
	}


	/**
	 * Obtiene una lista de todas las sesiones con plazas disponibles para una actividad específica.
	 * 
	 * @param actividad Nombre de la actividad de la cual se desean obtener las plazas disponibles.
	 * @return Un `JSONArray` con las sesiones disponibles, incluyendo día, hora y número de plazas.
	 *         Si no hay sesiones disponibles, se devuelve un JSONArray vacío.
	 */
	@SuppressWarnings("unchecked")
	public JSONArray listaPlazasDisponibles(String actividad) {
        // POR IMPLEMENTAR
		String keydia = "dia";
		JSONArray array = new JSONArray();
		Set<DiaSemana> semana = sesionesSemana.keySet();
		for(DiaSemana dia  : semana ) {
			Vector<Sesion> sesionesDia = sesionesSemana.get(dia);
			for(Sesion sesion : sesionesDia) {
				if(sesion.getActividad().equals(actividad)&&sesion.getPlazas()>=1) {
					JSONObject obj = sesion.toJSON();
					obj.put(keydia, dia);
					array.add(obj);
				}
			}
		}
		return array; // MODIFICAR
	}


	/**
	 * Realiza una reserva si hay la sesión existe y hay plazas disponibles.
	 * 
	 * @param codUsuario Código del usuario que solicita la reserva.
	 * @param actividad Nombre de la actividad que se quiere reservar.
	 * @param dia Día de la semana en que se quiere reservar la sesión.
	 * @param hora Hora de la sesión en formato 24 horas.
	 * @return Un objeto `JSONObject` con el código de la reserva si se realizó con éxito, o vacío si no fue posible reservar.
	 */
	@SuppressWarnings("unchecked")
	public JSONObject hazReserva(String codUsuario, String actividad, DiaSemana dia, long hora) {
        // POR IMPLEMENTA
	    Reserva nuevaReserva = new Reserva(codUsuario,actividad,dia,hora);
		if(indexarReserva(nuevaReserva)){
			return nuevaReserva.toJSON();
		}
		return new JSONObject();
	}


	/**
	 * Busca una reserva dentro de una lista de reservas de un usuario a partir de su código.
	 * 
	 * @param vector Lista de reservas asociadas a un usuario.
	 * @param codReserva Código único de la reserva que se desea buscar.
	 * @return La reserva encontrada o `null` si no existe una reserva con ese código.
	 */
	private Reserva buscaReserva(Vector<Reserva> vector, long codReserva) {
        // POR IMPLEMENTAR
		for(Reserva reservaUsuario : vector) {
			if(reservaUsuario.getCodReserva()==codReserva) {
				return reservaUsuario;
			}
		}
        return null; // MODIFICAR
	}




	/**
	 * Modifica una reserva existente, cambiándola a otro día y hora si hay plazas disponibles,
	 * pero manteniendo la actividad y el código de la reserva
	 *
	 * @param codUsuario Código del usuario que posee la reserva.
	 * @param codReserva Código de la reserva a modificar.
	 * @param nuevoDia Nuevo día de la semana para la reserva.
	 * @param nuevaHora Nueva hora de la sesión en formato 24 horas.
	 * @return Un `JSONObject` con la representación de la reserva modificada, o vacío si no se pudo modificar.
	 * falta agregrar la nueva plaza a la otra actividad
	 */
	public JSONObject modificaReserva(String codUsuario, long codReserva, DiaSemana nuevoDia, long nuevaHora) {
        // POR IMPLEMENTAR
		Vector<Reserva> vector = reservas.get(codUsuario);
	    if (vector == null) {
	        System.out.printf("El usuario %s no tiene reservas.\n", codUsuario);
	        return new JSONObject();
	    }
		Reserva reservaActual = buscaReserva(vector,codReserva);
		if(reservaActual != null ) {
			String actividad = reservaActual.getActividad();
			long horaAnterior = reservaActual.getHora();
			DiaSemana diaAnterior = reservaActual.getDia();
			Vector<Sesion> sesionesDia = sesionesSemana.get(nuevoDia);
			for(Sesion sesion : sesionesDia) {
				if(sesion.getActividad().equals(actividad)&&sesion.getPlazas()>0&&sesion.getHora()==nuevaHora) {
					Sesion sesionAnterior = buscaSesion(actividad,diaAnterior,horaAnterior);
					sesionAnterior.setPlazas(sesionAnterior.getPlazas()+1);
					sesion.setPlazas(sesion.getPlazas()-1);
					reservaActual.setHora(nuevaHora);
					reservaActual.setDia(nuevoDia);
					System.out.printf("Reserva modificada con exito \n ");
					System.out.printf("Reserva Modificada Actividad : %s - dia : %s - hora : %d",actividad,nuevoDia.toString(),nuevaHora);
					return reservaActual.toJSON();
				}
			}
		}
		
		System.out.printf("No existe ninguna reserva con este codigo %d \n ",codReserva);
        return new JSONObject(); // MODIFICAR
	}


	/**
	 * Cancela una reserva existente de un usuario y libera la plaza en la sesión correspondiente.
	 *
	 * @param codUsuario Código del usuario que ha hecho la reserva.
	 * @param codReserva Código único de la reserva a cancelar.
	 * @return Un `JSONObject` con la representación de la reserva cancelada, o vacío si no se encontró.
	 * falta agregar la nueva plaza a la actividad
	 */
	public JSONObject cancelaReserva(String codUsuario, long codReserva) {
        // POR IMPLEMENTAR
		Vector<Reserva> reservasUsuario = reservas.get(codUsuario);
	    if (reservasUsuario == null) {
	        System.out.printf("El usuario %s no tiene reservas.\n", codUsuario);
	        return new JSONObject();
	    }
		for(Reserva reserva : reservasUsuario) {
			if(reserva.getCodReserva() == codReserva) {
				String actividad = reserva.getActividad();
				long hora = reserva.getHora();
				DiaSemana dia = reserva.getDia();
				Sesion sesion = buscaSesion(actividad,dia,hora);
				sesion.setPlazas(sesion.getPlazas()+1);
				reservasUsuario.remove(reserva);
				System.out.printf("Tu reserva ha sido cancelada con exito \n ");
				System.out.printf("usuario : %s - Actividad : %s - dia : %s - hora  \n",codUsuario,actividad,hora );
				return reserva.toJSON();
			}
		}
		System.out.printf("No existe ninguna reserva de %s con este codigo de reserva  \n ",codUsuario,codReserva);
        return new JSONObject(); // MODIFICAR
	}

		private Boolean indexarReserva(Reserva reserva) {
			String actividad = reserva.getActividad();
			long hora = reserva.getHora();
			DiaSemana dia = reserva.getDia();
			String codUsuario = reserva.getCodUsuario();
			Sesion sesion = buscaSesion(actividad,dia,hora);
			if(sesion != null) {
				if(sesion.getPlazas()>0) {
					sesion.setPlazas(sesion.getPlazas()-1);
					Vector<Reserva> reservasUsuario = reservas.get(codUsuario);
					if(reservasUsuario==null) {
						reservasUsuario = new Vector<Reserva>();
						reservas.put(codUsuario, reservasUsuario);
					}
					reservasUsuario.add(reserva);
						return true;
					
					}
			}
			return false;
		
		}
}