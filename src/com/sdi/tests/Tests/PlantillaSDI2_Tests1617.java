package com.sdi.tests.Tests;
import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import com.sdi.tests.utils.SeleniumUtils;

//Ordenamos las pruebas por el nombre del método
@FixMethodOrder(MethodSorters.NAME_ASCENDING) 
public class PlantillaSDI2_Tests1617 {

	WebDriver driver; 
	List<WebElement> elementos = null;
	
	public PlantillaSDI2_Tests1617()
	{
	}

	@Before
	public void run()
	{
		//Este código es para ejecutar con la versión portale de Firefox 46.0
		File pathToBinary = new File("S:\\firefox\\FirefoxPortable.exe");
		FirefoxBinary ffBinary = new FirefoxBinary(pathToBinary);
		FirefoxProfile firefoxProfile = new FirefoxProfile();       
		driver = new FirefoxDriver(ffBinary,firefoxProfile);
//		driver.get("http://localhost:8180/sdi2-n");
		driver.get("http://localhost:8280/Notaneitor");									//TODO: dejar como estaba
		//Este código es para ejecutar con una versión instalada de Firex 46.0 
		//driver = new FirefoxDriver();
		//driver.get("http://localhost:8180/sdi2-n");			
	}
	@After
	public void end()
	{
		//Cerramos el navegador
		driver.quit();
	}
	
	/**
	 * Rellena el formulario indicado en formID, escribiendo en cada campo indicado por 
	 * la key del mapa params el texto correspondiente a su contenido. Envía el 
	 * formulario pulsando el botón indicado en buttonID
	 * @param formID id del formulario a rellenar
	 * @param buttonID id del botón a pulsar
	 * @param params mapa con pares nombre de campo - contenido
	 */
	private void fillForm(String formID, String buttonID, Map<String,String> params){
		for(String field : params.keySet()){
			WebElement nombre = driver.findElement(By.id(formID + ":" + field));
			nombre.click();
			nombre.clear();
			nombre.sendKeys(params.get(field));
		}
		//Pulsar el botón.
		By boton = By.id(formID + ":" + buttonID);
		driver.findElement(boton).click();	
	}

	//PRUEBAS
	//ADMINISTRADOR
	//PR01: Autentificar correctamente al administrador.
	@Test
    public void prueba01() {
		Map<String, String> params = new HashMap<>();
		params.put("login", "admin1");
		params.put("password", "admin1");
		
		fillForm("form-principal", "login-button", params);
		
		SeleniumUtils.EsperaCargaPagina(driver, "id", "form-users", 5);
    }
	//PR02: Fallo en la autenticación del administrador por introducir mal el login.
	@Test
    public void prueba02() {
		Map<String, String> params = new HashMap<>();
		params.put("login", "adminFail");
		params.put("password", "admin1");
		
		fillForm("form-principal", "login-button", params);
		
		SeleniumUtils.EsperaCargaPagina(driver, "id", "form-principal", 5);						//TODO: test más especifico?
    }
	//PR03: Fallo en la autenticación del administrador por introducir mal la password.
	@Test
    public void prueba03() {
		Map<String, String> params = new HashMap<>();
		params.put("login", "admin");
		params.put("password", "adminFail");
		
		fillForm("form-principal", "login-button", params);
		
		SeleniumUtils.EsperaCargaPagina(driver, "id", "form-principal", 5);						//TODO: test más especifico?
    }
	//PR04: Probar que la base de datos contiene los datos insertados con conexión correcta a la base de datos.
	@Test
    public void prueba04() {
		prueba01();
		By boton = By.id("form-cabecera:item-restoreDB");
		driver.findElement(boton).click();
		
		//AdminService aServ = Services
		
		assertTrue(false); //TODO miguel.
    }
	//PR05: Visualizar correctamente la lista de usuarios normales. 
	@Test
    public void prueba05() {
		prueba01();
		
		List<WebElement> usuarios=SeleniumUtils.EsperaCargaPaginaxpath(driver, "//table/tbody/tr/td[1]", 2);
		String[] nombreUsuarios={"user1","user2","user3"}; //TODO: Quizas completar comprobando + campos, por ahora solo nombres.
		
		int counter=0;
		for(WebElement usuario:usuarios){
			assertEquals(nombreUsuarios[counter],usuario.getText());
			counter++;
		}
    }
	//PR06: Cambiar el estado de un usuario de ENABLED a DISABLED. Y tratar de entrar con el usuario que se desactivado.
	@Test
    public void prueba06() {
		prueba01();
		
		//Primer clicamos en el user2 para deshabilitarlo
		List<WebElement> usuarios = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//table/tbody/tr[2]", 2);
		usuarios.get(0).click();
		
		//Lo deshabilitamos con el boton.
		List<WebElement> botonCambiarEstado = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//button[@id='form-users:table-users:status-button']", 2);
		botonCambiarEstado.get(0).click();
		
		//Comprobamos que aparece deshabilitado aqui.
		List<WebElement> estadoUsuario = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//table/tbody/tr[2]/td[4]", 2);
		assertEquals("Deshabilitado", estadoUsuario.get(0).getText());
		
		//TODO: comprobar que user2 no puede logearse(se necesita el boton log out.)
		WebElement botonCerrarSesión = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//a[contains(@id,'item-cerrarSesion')]", 2).get(0);
		botonCerrarSesión.click();
		
		Map<String, String> params = new HashMap<>();
		params.put("login", "user2");
		params.put("password", "user2");
		
		fillForm("form-principal", "login-button", params);
		
		SeleniumUtils.textoPresentePagina(driver, "Autentificación"); //Mejor otro tipo de via, TODO el nuevo mensaje para decir que no se puede.
		
		
    }
	//PR07: Cambiar el estado de un usuario a DISABLED a ENABLED. Y Y tratar de entrar con el usuario que se ha activado.
	@Test
    public void prueba07() {
		prueba01();
		
		//Primer clicamos en el user2 para deshabilitarlo
				List<WebElement> usuarios = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//table/tbody/tr[2]", 2);
				usuarios.get(0).click();
				
				//Lo deshabilitamos con el boton.
				List<WebElement> botonCambiarEstado = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//button[@id='form-users:table-users:status-button']", 2);
				botonCambiarEstado.get(0).click();
				
				//Comprobamos que aparece deshabilitado aqui.
				List<WebElement> estadoUsuario = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//table/tbody/tr[2]/td[4]", 2);
				assertEquals("Habilitado", estadoUsuario.get(0).getText());
				
				//TODO: comprobar que user2 PUEDE logearse(se necesita el boton log out.)
				WebElement botonCerrarSesión = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//a[contains(@id,'item-cerrarSesion')]", 2).get(0);
				botonCerrarSesión.click();
				
				Map<String, String> params = new HashMap<>();
				params.put("login", "user2");
				params.put("password", "user2");
				
				fillForm("form-principal", "login-button", params);
				
				SeleniumUtils.textoPresentePagina(driver, "Autentificación"); 
    }
	//PR08: Ordenar por Login
	@Test
    public void prueba08() {
		prueba01();
		
		//Hacemos click en la cabecera de login para ordenar por criterio
		WebElement loginButton = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//table/thead/tr/th[1]", 2).get(0);
		loginButton.click();
		
		//Por defecto ascendente, luego el orden es:
		String[] loginUsuariosEx = {"user1","user2","user3"};
		List<WebElement> loginUsuariosReal=SeleniumUtils.EsperaCargaPaginaxpath(driver, "//table/tbody/tr/td[1]", 2);
		int counter=0;
		for(WebElement loginUsuario: loginUsuariosReal){
			assertEquals(loginUsuariosEx[counter], loginUsuario.getText());
			counter++;
		}
		
		loginButton.click(); //Volvemos a pulsar para cambiar a orden inverso:
		loginUsuariosReal=SeleniumUtils.EsperaCargaPaginaxpath(driver, "//table/tbody/tr/td[1]", 2);
		counter=2; //Empezamos por el final del array
		for(WebElement loginUsuario: loginUsuariosReal){
			assertEquals(loginUsuariosEx[counter], loginUsuario.getText());
			counter--;
		}
		
    }
	//PR09: Ordenar por Email
	@Test
    public void prueba09() {
		prueba01();
		
		//Hacemos click en la cabecera de login para ordenar por criterio
		WebElement emailButton = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//table/thead/tr/th[2]", 2).get(0);
		emailButton.click();
		
		//Por defecto ascendente, luego el orden es:
		String[] loginUsuariosEx = {"user1","user2","user3"};
		List<WebElement> loginUsuariosReal=SeleniumUtils.EsperaCargaPaginaxpath(driver, "//table/tbody/tr/td[1]", 2);
		int counter=0;
		for(WebElement loginUsuario: loginUsuariosReal){
			assertEquals(loginUsuariosEx[counter], loginUsuario.getText());
			counter++;
		}
		
		emailButton.click(); //Volvemos a pulsar para cambiar a orden inverso:
		loginUsuariosReal=SeleniumUtils.EsperaCargaPaginaxpath(driver, "//table/tbody/tr/td[1]", 2);
		counter=2; //Empezamos por el final del array
		for(WebElement loginUsuario: loginUsuariosReal){
			assertEquals(loginUsuariosEx[counter], loginUsuario.getText());
			counter--;
		}
    }
	//PR10: Ordenar por Status
	@Test
    public void prueba10() {
		prueba01();
	
		//Primer clicamos en el user2 para deshabilitarlo
		List<WebElement> usuarios = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//table/tbody/tr[2]", 2);
		usuarios.get(0).click();
		
		//Lo deshabilitamos con el boton.
		List<WebElement> botonCambiarEstado = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//button[@id='form-users:table-users:status-button']", 2);
		botonCambiarEstado.get(0).click();
		
		//Comprobamos que aparece deshabilitado aqui.
		List<WebElement> estadoUsuario = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//table/tbody/tr[2]/td[4]", 2);
		assertEquals("Habilitado", estadoUsuario.get(0).getText());	
		//Hacemos click en la cabecera de login para ordenar por criterio
		WebElement statusButton = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//table/thead/tr/th[4]", 2).get(0);
		statusButton.click();
		
		///Se opta por comporobar por ambos login y enabled disabled.
		//Por defecto ascendente, luego el orden es:
		String[] loginUsuariosEx = {"user1","user3","user2"};
		String[] statusUsuariosEx = {"Habilitado","Habilitado","Deshabilitado"};
		List<WebElement> loginUsuariosReal=SeleniumUtils.EsperaCargaPaginaxpath(driver, "//table/tbody/tr/td[1]", 2);
		List<WebElement> statusUsuariosReal=SeleniumUtils.EsperaCargaPaginaxpath(driver, "//table/tbody/tr/td[4]", 2);
		int counter=0;
		for(WebElement loginUsuario: loginUsuariosReal){
			assertEquals(loginUsuariosEx[counter], loginUsuario.getText());
			assertEquals(statusUsuariosEx[counter], statusUsuariosReal.get(counter).getText());
			counter++;
		}
		
		statusButton.click(); //Volvemos a pulsar para cambiar a orden inverso:
		String[] loginUsuariosExSecondTime = {"user3","user1","user2"}; //La posicion del user1 se mantiene siempre la más alta luego hay que declararlo de nuevo.
		loginUsuariosReal=SeleniumUtils.EsperaCargaPaginaxpath(driver, "//table/tbody/tr/td[1]", 2);
		statusUsuariosReal=SeleniumUtils.EsperaCargaPaginaxpath(driver, "//table/tbody/tr/td[4]", 2);
		counter=2; //Empezamos por el final del array
		for(WebElement loginUsuario: loginUsuariosReal){
			assertEquals(loginUsuariosExSecondTime[counter], loginUsuario.getText());
			assertEquals(statusUsuariosEx[counter], statusUsuariosReal.get(	Math.abs(counter-2)).getText()); //TODO: quizas demasiado rebuscado y hacerlo mas claro
			counter--;
		}
		
		//Restauramos el orden previo para dejar el estado del usuario comoe estaba de la prueba07
		WebElement loginButton = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//table/thead/tr/th[1]", 2).get(0);
		loginButton.click();
		
		//Ponemos el user2 como estaba de nuevo.
		WebElement user2 = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//table/thead/tr[1]", 2).get(0);
		user2.click();
		
		List<WebElement> botonCambiarEstadoAfter = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//button[@id='form-users:table-users:status-button']", 2);
		botonCambiarEstadoAfter.get(0).click();
		
		
		
		
    }
	//PR11: Borrar una cuenta de usuario normal y datos relacionados.
	@Test
    public void prueba11() {
		prueba01();
		
		//Debe encontrarse el usuario antes de proseguir.
		SeleniumUtils.textoPresentePagina(driver, "user3@gmail.com");
		
		//Borramos el user3;
		WebElement user3 = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//table/tbody/tr[3]", 2).get(0);
		user3.click();
		
		WebElement botonBorrarUsuario = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//button[contains(@id,'delete-button')]", 2).get(0);
		botonBorrarUsuario.click();
		
		WebElement confirmationButton = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//button[contains(@id,'delete-yes')]", 2).get(0);
		confirmationButton.click();
		
		SeleniumUtils.textoNoPresentePagina(driver, "user3@gmail.com");	
		
		//Restauramos su estado anterior.
		By boton = By.id("form-cabecera:item-restoreDB");
		driver.findElement(boton).click();
		
    }
	//PR12: Crear una cuenta de usuario normal con datos válidos.
	@Test
    public void prueba12() {
		WebElement registro = driver.findElement(By.id("form-principal:register-button"));
		registro.click();
		SeleniumUtils.EsperaCargaPagina(driver, "id", "form-registro", 5);
		
		Map<String, String> params = new HashMap<>();
		params.put("login", "newuser");
		params.put("password", "newuser1");
		params.put("repeated-password", "newuser1");
		params.put("email", "newuser1@mail.com");
		
		fillForm("form-registro", "register-button", params);
		
		SeleniumUtils.EsperaCargaPagina(driver, "id", "form-principal", 5);						//TODO: test más especifico?
    }
	//PR13: Crear una cuenta de usuario normal con login repetido.
	@Test
    public void prueba13() {
		assertTrue(false); //TODO
    }
	//PR14: Crear una cuenta de usuario normal con Email incorrecto.
	@Test
    public void prueba14() {	//!!!\Comprobar que sale el mensaje de error y se sigue en la misma pagina
		WebElement registro = driver.findElement(By.id("form-principal:register-button"));
		registro.click();
		SeleniumUtils.EsperaCargaPagina(driver, "id", "form-registro", 5);
		
		Map<String, String> params = new HashMap<>();
		params.put("login", "newuser");
		params.put("password", "newuser1");
		params.put("repeated-password", "newuser1");
		params.put("email", "mailFail");
		
		fillForm("form-registro", "register-button", params);
		
		SeleniumUtils.EsperaCargaPagina(driver, "id", "form-registro", 5);
		SeleniumUtils.textoPresentePagina(driver, "El email no es válido");
    }
	//PR15: Crear una cuenta de usuario normal con Password incorrecta.
	@Test
    public void prueba15() {
		WebElement registro = driver.findElement(By.id("form-principal:register-button"));
		registro.click();
		SeleniumUtils.EsperaCargaPagina(driver, "id", "form-registro", 5);
		
		Map<String, String> params = new HashMap<>();
		params.put("login", "newuser");
		params.put("password", "new");
		params.put("repeated-password", "new");
		params.put("email", "newuser1@mail.com");
		
		fillForm("form-registro", "register-button", params);
		
		SeleniumUtils.EsperaCargaPagina(driver, "id", "form-registro", 5);
		SeleniumUtils.textoPresentePagina(driver, "La contraseña debe contener al menos 8 letras y números");
    }
	//USUARIO
	//PR16: Comprobar que en Inbox sólo aparecen listadas las tareas sin categoría y que son las que tienen que. Usar paginación navegando por las tres páginas.
	@Test
    public void prueba16() {
		assertTrue(false);
    }
	//PR17: Funcionamiento correcto de la ordenación por fecha planeada.
	@Test
    public void prueba17() {
		assertTrue(false);
    }
	//PR18: Funcionamiento correcto del filtrado.
	@Test
    public void prueba18() {
		assertTrue(false);
    }
	//PR19: Funcionamiento correcto de la ordenación por categoría.
	@Test
    public void prueba19() {
		assertTrue(false);
    }
	//PR20: Funcionamiento correcto de la ordenación por fecha planeada.
	@Test
    public void prueba20() {
		assertTrue(false);
    }
	//PR21: Comprobar que las tareas que no están en rojo son las de hoy y además las que deben ser.
	@Test
    public void prueba21() {
		assertTrue(false);
    }
	//PR22: Comprobar que las tareas retrasadas están en rojo y son las que deben ser.
	@Test
    public void prueba22() {
		assertTrue(false);
    }
	//PR23: Comprobar que las tareas de hoy y futuras no están en rojo y que son las que deben ser.
	@Test
    public void prueba23() {
		assertTrue(false);
    }
	//PR24: Funcionamiento correcto de la ordenación por día.
	@Test
    public void prueba24() {
		assertTrue(false);
    }
	//PR25: Funcionamiento correcto de la ordenación por nombre.
	@Test
    public void prueba25() {
		assertTrue(false);
    }
	//PR26: Confirmar una tarea, inhabilitar el filtro de tareas terminadas, ir a la pagina donde está la tarea terminada y comprobar que se muestra. 
	@Test
    public void prueba26() {
		assertTrue(false);
    }
	//PR27: Crear una tarea sin categoría y comprobar que se muestra en la lista Inbox.
	@Test
    public void prueba27() {
		assertTrue(false);
    }
	//PR28: Crear una tarea con categoría categoria1 y fecha planeada Hoy y comprobar que se muestra en la lista Hoy.
	@Test
    public void prueba28() {
		assertTrue(false);
    }
	//PR29: Crear una tarea con categoría categoria1 y fecha planeada posterior a Hoy y comprobar que se muestra en la lista Semana.
	@Test
    public void prueba29() {
		assertTrue(false);
    }
	//PR30: Editar el nombre, y categoría de una tarea (se le cambia a categoría1) de la lista Inbox y comprobar que las tres pseudolista se refresca correctamente.
	@Test
    public void prueba30() {
		assertTrue(false);
    }
	//PR31: Editar el nombre, y categoría (Se cambia a sin categoría) de una tarea de la lista Hoy y comprobar que las tres pseudolistas se refrescan correctamente.
	@Test
    public void prueba31() {
		assertTrue(false);
    }
	//PR32: Marcar una tarea como finalizada. Comprobar que desaparece de las tres pseudolistas.
	@Test
    public void prueba32() {
		assertTrue(false);
    }
	//PR33: Salir de sesión desde cuenta de administrador.
	@Test
    public void prueba33() {
		prueba01();
		
		//Hacemos click en el boton de cerrar sesión.
		WebElement botonCerrarSesión = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//a[contains(@id,'item-cerrarSesion')]", 2).get(0);
		botonCerrarSesión.click();
		
		//Comrpobamos que estamos en la página de login.
		SeleniumUtils.textoPresentePagina(driver, "Autentificación"); 
		
    }
	//PR34: Salir de sesión desde cuenta de usuario normal.
	@Test
    public void prueba34() {
		assertTrue(false);
    }
	//PR35: Cambio del idioma por defecto a un segundo idioma. (Probar algunas vistas)
	@Test
    public void prueba35() {
		assertTrue(false);
    }
	//PR36: Cambio del idioma por defecto a un segundo idioma y vuelta al idioma por defecto. (Probar algunas vistas)
	@Test
    public void prueba36() {
		assertTrue(false);
    }
	//PR37: Intento de acceso a un  URL privado de administrador con un usuario autenticado como usuario normal.
	@Test
    public void prueba37() {
		//TODO: cuando miguel haga su parte.
		assertTrue(false);
    }
	//PR38: Intento de acceso a un  URL privado de usuario normal con un usuario no autenticado.
	@Test
    public void prueba38() {
		prueba01(); //Nos logeamos como administrador
		
		//Intentamos acceder a una de las paginas de usuario
		driver.get("http://localhost:8280/Notaneitor/user/tasks.xhtml");
		//Te tiene que redireccionar a login
		
		SeleniumUtils.textoPresentePagina(driver, "Autentificación");
    }

	



	
	


    
}