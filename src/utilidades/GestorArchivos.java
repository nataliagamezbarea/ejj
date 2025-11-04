package utilidades;

import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * Clase utilitaria para gestionar archivos de reservas.
 * Permite crear archivos, escribir líneas, leer datos y procesar errores.
 * Todas las funciones manejan posibles excepciones y dan mensajes claros al usuario.
 */
public class GestorArchivos {

    /**
     * Crea un archivo si no existe. Si el archivo ya existía, no lo sobrescribe.
     *
     * @param nombre_archivo Nombre completo del archivo a crear.
     */
    public static void crearArchivo(String nombre_archivo) {
        try {
            File archivo = new File(nombre_archivo);
            FileWriter escribir = new FileWriter(archivo, true);
            escribir.close();
        } catch (SecurityException e) {
            System.out.println("No tienes permisos para escribir en esta carpeta");
        } catch (NullPointerException e) {
            System.out.println("El nombre del fichero no puede ser nulo");
        } catch (IOException e) {
            System.out.println("Ha habido problemas de entrada y salida: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Ha habido problemas generales: " + e.getMessage());
        }
    }

    /**
     * Escribe una línea en un archivo existente. Añade salto de línea automáticamente.
     *
     * @param nombre_archivo Archivo donde se escribirá la línea.
     * @param linia          Texto a escribir en el archivo.
     */
    public static void escribirArchivo(String nombre_archivo, String linia) {
        try {
            FileWriter escritor = new FileWriter(nombre_archivo, true);
            escritor.write(linia + "\n");
            escritor.close();
        } catch (FileNotFoundException e) {
            System.out.println("El archivo no existe");
        } catch (SecurityException e) {
            System.out.println("No tienes permisos para escribir en esta carpeta");
        } catch (NullPointerException e) {
            System.out.println("El nombre del fichero no puede ser nulo");
        } catch (IOException e) {
            System.out.println("Ha habido problemas de entrada y salida: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Ha habido problemas generales: " + e.getMessage());
        }
    }

    /**
     * Comprueba si un archivo está vacío y le añade encabezados si es necesario.
     *
     * @param nombre_archivo Nombre del archivo a revisar.
     * @param encabezados    Texto de los encabezados separados por coma.
     */
    public static void comprobarEncabezados(String nombre_archivo, String encabezados) {
        File archivo = new File(nombre_archivo);
        if (archivo.length() == 0) {
            GestorArchivos.escribirArchivo(nombre_archivo, encabezados);
        }
    }

    /**
     * Lee un archivo de reservas y crea instancias de la clase Reserva.
     * Cada reserva creada se agrega automáticamente al GestorReservas.
     *
     * @param nombre_archivo Ruta completa del archivo de reservas.
     */
    public static void leeryCrearInstanciasDesdeArchivo(String nombre_archivo) {
        try (BufferedReader lector = new BufferedReader(new FileReader(nombre_archivo))) {
            // Leer encabezados
            String primeraLinea = lector.readLine();
            if (primeraLinea == null) {
                System.out.println("El archivo está vacío.");
                return;
            }

            List<String> listaColumnas = Arrays.asList(primeraLinea.split(","));

            int numeroAsientoCol = listaColumnas.indexOf("NumeroAsiento");
            int nombrePasajeroCol = listaColumnas.indexOf("NombrePasajero");
            int claseCol = listaColumnas.indexOf("Clase");
            int destinoCol = listaColumnas.indexOf("Destino"); // Puede no existir

            if (numeroAsientoCol == -1 || nombrePasajeroCol == -1 || claseCol == -1) {
                System.out.println("Error: el archivo debe contener los encabezados 'NumeroAsiento,NombrePasajero,Clase'.");
                return;
            }

            String linea;
            while ((linea = lector.readLine()) != null) {
                String[] datos = linea.split(",");

                // Crear la reserva según si tiene destino o no
                Reserva r;
                if (destinoCol != -1 && destinoCol < datos.length) {
                    r = new Reserva(
                            datos[numeroAsientoCol].trim(),
                            datos[nombrePasajeroCol].trim(),
                            datos[claseCol].trim(),
                            datos[destinoCol].trim()
                    );
                } else {
                    r = new Reserva(
                            datos[numeroAsientoCol].trim(),
                            datos[nombrePasajeroCol].trim(),
                            datos[claseCol].trim()
                    );
                }

                // Agregar la reserva al GestorReservas
                GestorReservas.agregarReserva(r);
            }

        } catch (FileNotFoundException e) {
            System.out.println("El archivo no existe o la ruta es incorrecta.");
        } catch (IOException e) {
            System.out.println("Ha habido problemas de entrada y salida: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Ha habido un error inesperado: " + e.getMessage());
        }
    }

    /**
     * Procesa un archivo de reservas que puede contener errores.
     * <p>
     * Funcionalidad:
     * - Valida que cada línea tenga 4 campos: NumeroAsiento, NombrePasajero, Clase, Destino.
     * - Las reservas válidas se guardan en archivos por destino.
     * - Las reservas con errores se registran en registro_errores.log junto con la descripción del error y la fecha.
     *
     * @param archivoMaestro Archivo de entrada que puede contener reservas con errores.
     */
    public static void procesarReservasConErrores(String archivoMaestro) {
        String archivoErrores = "src/Ejercicio3/registro_errores.log";
        crearArchivo(archivoErrores);

        try (BufferedReader lector = new BufferedReader(new FileReader(archivoMaestro))) {

            String linea;
            while ((linea = lector.readLine()) != null) {
                String[] datos = linea.split(",");
                String[] nombresCampos = {"NumeroAsiento", "NombrePasajero", "Clase", "Destino"};

                String[] datosCompletos = new String[4];
                for (int i = 0; i < 4; i++) {
                    if (i < datos.length) {
                        datosCompletos[i] = datos[i].trim();
                    } else {
                        datosCompletos[i] = "";
                    }
                }

                boolean lineaValida = true;
                StringBuilder descripcionError = new StringBuilder();

                for (int i = 0; i < 4; i++) {
                    if (datosCompletos[i].isEmpty()) {
                        lineaValida = false;
                        if (!descripcionError.isEmpty()) descripcionError.append("; ");
                        descripcionError.append("Falta el campo '").append(nombresCampos[i]).append("'");
                    }
                }

                if (!lineaValida) {
                    String fechaHora = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
                    escribirArchivo(archivoErrores, fechaHora + ", " + linea + ", " + descripcionError);
                    continue;
                }

                // Crear instancia de reserva válida
                Reserva r = new Reserva(datosCompletos[0], datosCompletos[1], datosCompletos[2], datosCompletos[3]);
                GestorReservas.agregarReserva(r); // Aquí se incrementa el contador

                // Guardar reserva válida en archivo por destino
                String archivoPorDestino = "src/Ejercicio3/reserva_" + datosCompletos[3].toLowerCase() + ".txt";
                crearArchivo(archivoPorDestino);
                escribirArchivo(archivoPorDestino, linea);
            }

        } catch (Exception e) {
            System.out.println("Ha ocurrido un error: " + e.getMessage());
        }
    }

    /**
     * Muestra en consola el contenido de un archivo de registro de errores.
     *
     * @param archivoErrores Ruta del archivo de errores.
     */
    public static void mostrarRegistroErrores(String archivoErrores) {
        try (BufferedReader lector = new BufferedReader(new FileReader(archivoErrores))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                System.out.println(linea);
            }
        } catch (FileNotFoundException e) {
            System.out.println("El archivo de errores no existe.");
        } catch (IOException e) {
            System.out.println("Error de entrada/salida: " + e.getMessage());
        }
    }
}
