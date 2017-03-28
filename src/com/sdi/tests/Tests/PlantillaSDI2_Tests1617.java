package com.sdi.tests.Tests;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import alb.util.date.DateUtil;

import com.sdi.business.AdminService;
import com.sdi.business.Services;
import com.sdi.business.TaskService;
import com.sdi.business.exception.BusinessException;
import com.sdi.comparator.TaskComparator;
import com.sdi.dto.Category;
import com.sdi.dto.Task;
import com.sdi.dto.User;
import com.sdi.persistence.impl.TaskDaoJdbcImpl;
import com.sdi.tests.utils.SeleniumUtils;

//Ordenamos las pruebas por el nombre del método
@FixMethodOrder(MethodSorters.NAME_ASCENDING) 
public class PlantillaSDI2_Tests1617 {

	static WebDriver driver = getDriver(); 
	static String url = "http://localhost:8280/Notaneitor";		//TODO: dejar como estaba
	
	static DateFormat df = getDf();
	
	
	public PlantillaSDI2_Tests1617()
	{
	}

	@Before
	public void run()
	{
		
		
//		driver.get("http://localhost:8180/sdi2-n");
		driver.navigate().to(url);															//TODO: dejar como estaba
		//Este código es para ejecutar con una versión instalada de Firex 46.0 
		//driver = new FirefoxDriver();
		//driver.get("http://localhost:8180/sdi2-n");			
	}
	
	public static WebDriver getDriver(){
		//Este código es para ejecutar con la versión portale de Firefox 46.0
		File pathToBinary = new File("S:\\firefox\\FirefoxPortable.exe");
		FirefoxBinary ffBinary = new FirefoxBinary(pathToBinary);
		FirefoxProfile firefoxProfile = new FirefoxProfile(); 
		return new FirefoxDriver(ffBinary,firefoxProfile);
	}
	
	public static DateFormat getDf(){
		return DateFormat.getDateInstance(DateFormat.FULL);
	}
	
	@After
	public void end()
	{
		//Cerramos el navegador
//		driver.quit();
		driver.manage().deleteAllCookies();
	}
	
	@AfterClass
	static public void endAll() {
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
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
	
	private List<WebElement> CustomEsperaCargaPaginaxpath(WebDriver driver, String xpath, int timeout){
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return SeleniumUtils.EsperaCargaPaginaxpath(driver, xpath, timeout);
	}
	
	private void loginUser(){
		Map<String, String> params = new HashMap<>();
		params.put("login", "user1");
		params.put("password", "user1");
		
		fillForm("form-principal", "login-button", params);
	}
	private void loginAdmin(){
		Map<String, String> params = new HashMap<>();
		params.put("login", "admin1");
		params.put("password", "admin1");
		
		fillForm("form-principal", "login-button", params);
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
    public void prueba04() throws BusinessException {
		prueba01();
		By boton = By.id("form-cabecera:item-restoreDB");
		driver.findElement(boton).click();
		
		//USUARIOS
		AdminService aServ = Services.getAdminService();
		
		List<User> expectedUsers= new ArrayList<User>();
		
		expectedUsers.add(new User().setId(new Long(4)).setLogin("admin1").setPassword("admin1")
				.setEmail("admin1@gmail.com").setIsAdmin(true));
		
		for(int i=1; i<=3;i++){
			expectedUsers.add(new User().setLogin("user"+i).setPassword("user"+i).setEmail("user"+i+"@gmail.com"));
		}
		
		
		List<User> users = aServ.findAllUsers();
		
		int counter=0;
		for(User u: users){
			//Copiando las ids ya que se generan dinamicamente.
			User expectedUser = expectedUsers.get(counter);
			expectedUser.setId(u.getId());
			assertEquals(expectedUser, u);
			counter++;
		}
		
		//CATEGORIAS
		
		TaskService tServ = Services.getTaskService();
		
		List<Category> expectedCategories = new ArrayList<Category>();
		
		counter=0;
		for (User u : users) {
			if (!u.getIsAdmin()) {
				for (int i = 1; i <= 3; i++) {
					Category categ = new Category();
					expectedCategories.add(categ.setId(new Long(counter))
							.setUserId(u.getId())
							.setName("Category" + counter));
					expectedCategories.add(categ);
					counter++;
				}
			}
		}
		
		List<Category> categories = new ArrayList<Category>();
		//Cogemos todas las tareas
		for(User u: users){
			categories.addAll(tServ.findCategoriesByUserId(u.getId()));
		}
		
		counter=0;
		for(Category c: categories){
			//Copiando las ids ya que se generan dinamicamente.
			Category expectedCategory = categories.get(counter);
			expectedCategory.setId(c.getId());
			assertEquals(expectedCategory,c);
			counter++;
		}
		
		//TAREAS
		List<Task> expectedTasks = new ArrayList<Task>();
		
		counter = 1;
		for (User u : users) {
			if (!u.getIsAdmin()) {
				
				List<Category> categoriasUsuario = tServ
						.findCategoriesByUserId(u.getId());
				for (int i = 1; i <= 10; i++) {
					Task tarea = new Task();
					expectedTasks.add(tarea
							.setPlanned(
									DateUtil.addDays(DateUtil.today(), 6))
							.setId(new Long(counter)).setTitle("Tarea" + counter)
							.setUserId(u.getId()));
					counter++;
				}
				for (int i = 1; i <= 10; i++) {
					Task tarea = new Task();
					expectedTasks.add(tarea.setPlanned(DateUtil.today())
							.setId(new Long(counter)).setTitle("Tarea" + counter)
							.setUserId(u.getId()));
					counter++;
				}
				for (int i = 1; i <= 10; i++) {
					Task tarea = new Task();
					Category categ = null;
					if (i <= 3) {
						categ = categoriasUsuario.get(0);
					} else {
						if (i <= 6) {
							categ = categoriasUsuario.get(1);
						} else {
							if (i <= 10) {
								categ = categoriasUsuario.get(2);
							}
						}

					}

					expectedTasks.add(tarea
							.setPlanned(DateUtil.addDays(DateUtil.today(), -counter))
							.setId(new Long(counter)).setTitle("Tarea" + counter)
							.setUserId(u.getId())
							.setCategoryId(categ.getId()));
					counter++;
				}
			
			}
		}
		
		//Fueron introducidas en desorden, debemos ordenarlas, como sabemos a priori
		//el formato de las tareas de pruebas(task+nº) podemos comparar por ese titulo.
		Collections.sort(expectedTasks, new TaskComparator());
		
		TaskDaoJdbcImpl tDao = new TaskDaoJdbcImpl();
		List<Task> tasks = new ArrayList<Task>();

		//Cogemos todas las tareas que existen en la base de datos.
		tasks.addAll(tDao.findAll());
		
		//Tambien ordenamos estas:
		Collections.sort(tasks, new TaskComparator());
		
		counter=0;
		for(Task t: tasks){
			//Copiando las ids ya que se generan dinamicamente.
			Task expectedTask = expectedTasks.get(counter);
			expectedTask.setId(t.getId());
			assertEquals(expectedTask,t);
			counter++;
			
		}
		
		
		
    }
	//PR05: Visualizar correctamente la lista de usuarios normales. 
	@Test
    public void prueba05() throws InterruptedException {
		loginAdmin();
		Thread.sleep(500);
		List<WebElement> usuarios=CustomEsperaCargaPaginaxpath(driver, "//table/tbody/tr/td[1]", 2);
		String[] nombreUsuarios={"user1","user2","user3"}; //TODO: Quizas completar comprobando + campos, por ahora solo nombres.
		
		int counter=0;
		for(WebElement usuario:usuarios){
			assertEquals(nombreUsuarios[counter],usuario.getText());
			counter++;
		}
    }
	//PR06: Cambiar el estado de un usuario de ENABLED a DISABLED. Y tratar de entrar con el usuario que se desactivado.
	@Test
    public void prueba06() throws InterruptedException {
		loginAdmin();
		Thread.sleep(500);
		
		//Primer clicamos en el user2 para deshabilitarlo
		List<WebElement> usuarios = CustomEsperaCargaPaginaxpath(driver, "//table/tbody/tr[2]", 2);
		usuarios.get(0).click();
		
		//Lo deshabilitamos con el boton.
		int attempts =0;
		List<WebElement> botonCambiarEstado = null;
		while(attempts <2){
			try{
				botonCambiarEstado = CustomEsperaCargaPaginaxpath(driver, "//button[contains(@id, 'status-button')]", 2);
			}catch(StaleElementReferenceException e){
				
			}
			attempts++;
		}
		
		botonCambiarEstado.get(0).click();
		
		//Comprobamos que aparece deshabilitado aqui.
		attempts =0;
		List<WebElement> estadoUsuario = null;
		while(attempts <2){
			try{
				estadoUsuario = CustomEsperaCargaPaginaxpath(driver, "//table/tbody/tr[2]/td[4]", 2);
			}catch(StaleElementReferenceException e){
				
			}
			attempts++;
		}
		assertEquals("Deshabilitado", estadoUsuario.get(0).getText());
		
		//TODO: comprobar que user2 no puede logearse(se necesita el boton log out.)
		WebElement botonCerrarSesión = CustomEsperaCargaPaginaxpath(driver, "//button[contains(@id,'item-cerrarSesion')]", 2).get(0);
		botonCerrarSesión.click();
		
		Thread.sleep(500);
		
		Map<String, String> params = new HashMap<>();
		params.put("login", "user2");
		params.put("password", "user2");
		
		fillForm("form-principal", "login-button", params);
		
		SeleniumUtils.textoPresentePagina(driver, "Autentificación"); //Mejor otro tipo de via, TODO el nuevo mensaje para decir que no se puede.
		
		
    }
	//PR07: Cambiar el estado de un usuario a DISABLED a ENABLED. Y Y tratar de entrar con el usuario que se ha activado.
	@Test
    public void prueba07() throws InterruptedException {
		loginAdmin();

		//Primer clicamos en el user2 para deshabilitarlo
				List<WebElement> usuarios = CustomEsperaCargaPaginaxpath(driver, "//table/tbody/tr[2]", 2);
				usuarios.get(0).click();

				//Lo deshabilitamos con el boton.
				List<WebElement> botonCambiarEstado = CustomEsperaCargaPaginaxpath(driver, "//button[@id='form-users:table-users:status-button']", 2);
				botonCambiarEstado.get(0).click();
				
				//Comprobamos que aparece deshabilitado aqui.
				List<WebElement> estadoUsuario = CustomEsperaCargaPaginaxpath(driver, "//table/tbody/tr[2]/td[4]", 2);
				assertEquals("Habilitado", estadoUsuario.get(0).getText());
				
				//TODO: comprobar que user2 PUEDE logearse(se necesita el boton log out.)
				WebElement botonCerrarSesión = CustomEsperaCargaPaginaxpath(driver, "//button[contains(@id,'item-cerrarSesion')]", 2).get(0);
				botonCerrarSesión.click();
				
				Map<String, String> params = new HashMap<>();
				params.put("login", "user2");
				params.put("password", "user2");
				
				fillForm("form-principal", "login-button", params);
				
				//Hay que esperar a que se cargue
				Thread.sleep(1000);
				SeleniumUtils.textoPresentePagina(driver, "Listado de tareas"); 
    }
	//PR08: Ordenar por Login
	@Test
    public void prueba08() {
		loginAdmin();
		
		//Hacemos click en la cabecera de login para ordenar por criterio
		WebElement loginButton = CustomEsperaCargaPaginaxpath(driver, "//table/thead/tr/th[1]", 2).get(0);
		loginButton.click();
		
		//Por defecto ascendente, luego el orden es:
		String[] loginUsuariosEx = {"user1","user2","user3"};
		List<WebElement> loginUsuariosReal=CustomEsperaCargaPaginaxpath(driver, "//table/tbody/tr/td[1]", 2);
		int counter=0;
		for(WebElement loginUsuario: loginUsuariosReal){
			assertEquals(loginUsuariosEx[counter], loginUsuario.getText());
			counter++;
		}
		
		loginButton.click(); //Volvemos a pulsar para cambiar a orden inverso:
		loginUsuariosReal=CustomEsperaCargaPaginaxpath(driver, "//table/tbody/tr/td[1]", 2);
		counter=2; //Empezamos por el final del array
		for(WebElement loginUsuario: loginUsuariosReal){
			assertEquals(loginUsuariosEx[counter], loginUsuario.getText());
			counter--;
		}
		
    }
	//PR09: Ordenar por Email
	@Test
    public void prueba09() {
		loginAdmin();
		
		//Hacemos click en la cabecera de login para ordenar por criterio
		WebElement emailButton = CustomEsperaCargaPaginaxpath(driver, "//table/thead/tr/th[2]", 2).get(0);
		emailButton.click();
		
		//Por defecto ascendente, luego el orden es:
		String[] loginUsuariosEx = {"user1","user2","user3"};
		List<WebElement> loginUsuariosReal=CustomEsperaCargaPaginaxpath(driver, "//table/tbody/tr/td[1]", 2);
		int counter=0;
		for(WebElement loginUsuario: loginUsuariosReal){
			assertEquals(loginUsuariosEx[counter], loginUsuario.getText());
			counter++;
		}
		
		emailButton.click(); //Volvemos a pulsar para cambiar a orden inverso:
		loginUsuariosReal=CustomEsperaCargaPaginaxpath(driver, "//table/tbody/tr/td[1]", 2);
		counter=2; //Empezamos por el final del array
		for(WebElement loginUsuario: loginUsuariosReal){
			assertEquals(loginUsuariosEx[counter], loginUsuario.getText());
			counter--;
		}
    }
	//PR10: Ordenar por Status
	@Test
    public void prueba10() throws InterruptedException {
		loginAdmin();
	
		//Primer clicamos en el user2 para deshabilitarlo
		Thread.sleep(500);
		List<WebElement> usuarios = CustomEsperaCargaPaginaxpath(driver, "//table/tbody/tr[2]", 2);
		usuarios.get(0).click();
		
		//Lo deshabilitamos con el boton.
		List<WebElement> botonCambiarEstado = CustomEsperaCargaPaginaxpath(driver, "//button[@id='form-users:table-users:status-button']", 2);
		botonCambiarEstado.get(0).click();
		
		//Comprobamos que aparece deshabilitado aqui.
		List<WebElement> estadoUsuario = CustomEsperaCargaPaginaxpath(driver, "//table/tbody/tr[2]/td[4]", 2);
		assertEquals("Deshabilitado", estadoUsuario.get(0).getText());	
		//Hacemos click en la cabecera de login para ordenar por criterio
		WebElement statusButton = CustomEsperaCargaPaginaxpath(driver, "//table/thead/tr/th[4]", 2).get(0);
		statusButton.click();
		
		///Se opta por comporobar por ambos login y enabled disabled.
		//Por defecto ascendente, luego el orden es:
		String[] loginUsuariosEx = {"user1","user3","user2"};
		String[] statusUsuariosEx = {"Habilitado","Habilitado","Deshabilitado"};
		List<WebElement> loginUsuariosReal=CustomEsperaCargaPaginaxpath(driver, "//table/tbody/tr/td[1]", 2);
		List<WebElement> statusUsuariosReal=CustomEsperaCargaPaginaxpath(driver, "//table/tbody/tr/td[4]", 2);
		int counter=0;
		for(WebElement loginUsuario: loginUsuariosReal){
			assertEquals(loginUsuariosEx[counter], loginUsuario.getText());
			assertEquals(statusUsuariosEx[counter], statusUsuariosReal.get(counter).getText());
			counter++;
		}
		
		statusButton.click(); //Volvemos a pulsar para cambiar a orden inverso:
		String[] loginUsuariosExSecondTime = {"user3","user1","user2"}; //La posicion del user1 se mantiene siempre la más alta luego hay que declararlo de nuevo.
		loginUsuariosReal=CustomEsperaCargaPaginaxpath(driver, "//table/tbody/tr/td[1]", 2);
		statusUsuariosReal=CustomEsperaCargaPaginaxpath(driver, "//table/tbody/tr/td[4]", 2);
		counter=2; //Empezamos por el final del array
		for(WebElement loginUsuario: loginUsuariosReal){
			assertEquals(loginUsuariosExSecondTime[counter], loginUsuario.getText());
			assertEquals(statusUsuariosEx[counter], statusUsuariosReal.get(	Math.abs(counter-2)).getText()); //TODO: quizas demasiado rebuscado y hacerlo mas claro
			counter--;
		}
		
		//Restauramos el orden previo para dejar el estado del usuario comoe estaba de la prueba07
		WebElement loginButton = CustomEsperaCargaPaginaxpath(driver, "//table/thead/tr/th[1]", 2).get(0);
		loginButton.click();
		
		//Ponemos el user2 como estaba de nuevo.
		WebElement user2 = CustomEsperaCargaPaginaxpath(driver, "//table/thead/tr[1]", 2).get(0);
		user2.click();
		
		List<WebElement> botonCambiarEstadoAfter = CustomEsperaCargaPaginaxpath(driver, "//button[@id='form-users:table-users:status-button']", 2);
		botonCambiarEstadoAfter.get(0).click();
		
		
		
		
    }
	//PR11: Borrar una cuenta de usuario normal y datos relacionados.
	@Test
    public void prueba11() throws InterruptedException {
		loginAdmin();
		//Necesitamos esperar un poco.
		Thread.sleep(500);
		//Debe encontrarse el usuario antes de proseguir.
		SeleniumUtils.textoPresentePagina(driver, "user3@gmail.com");
		
		//Borramos el user3;
		WebElement user3 = CustomEsperaCargaPaginaxpath(driver, "//table/tbody/tr[3]", 2).get(0);
		user3.click();
		
		WebElement botonBorrarUsuario = CustomEsperaCargaPaginaxpath(driver, "//button[contains(@id,'delete-button')]", 2).get(0);
		botonBorrarUsuario.click();
		
		WebElement confirmationButton = CustomEsperaCargaPaginaxpath(driver, "//button[contains(@id,'delete-yes')]", 2).get(0);
		confirmationButton.click();
		
		Thread.sleep(500);
		
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
		WebElement registro = driver.findElement(By.id("form-principal:register-button"));
		registro.click();
		SeleniumUtils.EsperaCargaPagina(driver, "id", "form-registro", 5);
		
		Map<String, String> params = new HashMap<>();
		params.put("login", "user1");
		params.put("password", "PasswordPrueba1");
		params.put("repeated-password", "PasswordPrueba1");
		params.put("email", "EstoVaAFallar@gmail.com");
		
		fillForm("form-registro", "register-button", params);
		
		SeleniumUtils.EsperaCargaPagina(driver, "id", "form-registro", 5);
		SeleniumUtils.textoPresentePagina(driver, "Ya existe un usuario con este nombre");
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
		loginUser();
		//Las tareas Inbox correspondientes al usuario1 (sin categoria) son de la 1 a la 20 (20-30 con categoria)
		Map<String,String[]> inboxTasksHoy = new HashMap<String,String[]>();
		//Primero añadimos las que tienen fecha planeada más atrasada(aparecen así por defecto) tareas de la 11 a la 20
		for(int i=11;i<=20;i++){
			String[] datosAMirar = {"Tarea"+i,df.format(DateUtil.today()),df.format(DateUtil.today())};
			inboxTasksHoy.put("Tarea"+i, datosAMirar);
		}
		Map<String,String[]> inboxTasks = new HashMap<String,String[]>();
		//A continuación las que no estan retrasadas 1-10
		for(int i=1;i<=10;i++){
			String[] datosAMirar = {"Tarea"+i,df.format(DateUtil.today()),df.format(DateUtil.addDays(DateUtil.today(), 6))};
			inboxTasks.put("Tarea"+i,datosAMirar);
		}
		int rowCount=1;
		//Cogemos todas las tareas
		for(int i=1;i<=20;i++){
			if(i==9){
				//Debemos pasar a la sigueinte página.
				WebElement page = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//span[@class='ui-paginator-pages']/a[2]", 2).get(0);
				page.click();
				rowCount=1;
			}
			if(i==17){
				//Debemos pasar a la sigueinte página.
				WebElement page = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//span[@class='ui-paginator-pages']/a[3]", 2).get(0);
				page.click();
				rowCount=1;
			}
			String nombre ="";
			String creacion="";
			String planeada="";
			
			int attempts =0;
			while(attempts <2){
				try{
					nombre = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//table/tbody/tr["+rowCount+"]/td[1]", 2).get(0).getText();
					creacion = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//table/tbody/tr["+rowCount+"]/td[2]", 2).get(0).getText();
					planeada = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//table/tbody/tr["+rowCount+"]/td[3]", 2).get(0).getText();
				}catch(StaleElementReferenceException e){
					
				}
				attempts++;
			}
			
			
			String[] parsedTask = {nombre,creacion,planeada};
			if(i<=10){
				//Las 20 primeras retrasadas
				//Tenemos que tener en cuenta que el orden no es fijo, usamos el mapa
				assertArrayEquals(inboxTasksHoy.get(parsedTask[0]),parsedTask);
			}
			else{
				//Las 10 ultimas no retrasadas
				assertArrayEquals(inboxTasks.get(parsedTask[0]),parsedTask);
			}
			rowCount++;
		}
		
    }
	//PR17: Funcionamiento correcto de la ordenación por fecha planeada.
	@Test
    public void prueba17() {
		//Ya vienen ordenadas por defecto pero se demuestra de todas formas
		loginUser();
		
		List<String> fechaPlaneadaEsperadaHoy = new ArrayList<String>();
		//Añadimos las de hoy 11-20
		for(int i=11;i<=20;i++){
			fechaPlaneadaEsperadaHoy.add(df.format(DateUtil.today()));
		}
		List<String> fechaPlaneadaEsperadaFuturo = new ArrayList<String>();
		//Añadimos las del futuro 1-10
		for(int i=1;i<=10;i++){
			fechaPlaneadaEsperadaFuturo.add(df.format(DateUtil.addDays(DateUtil.today(), 6)));
		}
		
		int rowCount=1;
		//Cogemos todas las tareas
		for(int i=1;i<=20;i++){
			if(i==9){
				//Debemos pasar a la sigueinte página.
				WebElement page = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//span[@class='ui-paginator-pages']/a[2]", 2).get(0);
				page.click();
				rowCount=1;
			}
			if(i==17){
				//Debemos pasar a la sigueinte página.
				WebElement page = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//span[@class='ui-paginator-pages']/a[3]", 2).get(0);
				page.click();
				rowCount=1;
			}
			String planeada="";
			
			int attempts =0;
			while(attempts <2){
				try{
					planeada = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//table/tbody/tr["+rowCount+"]/td[3]", 2).get(0).getText();
				}catch(StaleElementReferenceException e){
					
				}
				attempts++;
			}
			
			if(i<=10){
				//Las 20 primeras retrasadas
				//Tenemos que tener en cuenta que el orden no es fijo, usamos el mapa
				assertTrue(fechaPlaneadaEsperadaHoy.contains(planeada));
				assertTrue(!fechaPlaneadaEsperadaFuturo.contains(planeada));
			}
			else{
				//Las 10 ultimas no retrasadas
				assertTrue(!fechaPlaneadaEsperadaHoy.contains(planeada));
				assertTrue(fechaPlaneadaEsperadaFuturo.contains(planeada));
			}
			rowCount++;
		}
		
    }
	//PR18: Funcionamiento correcto del filtrado.
	@Test
    public void prueba18() throws InterruptedException {
		loginUser();
		//Cogemos el input del filtro e introducimos el filtrado
		WebElement inputFiltroTitulo = CustomEsperaCargaPaginaxpath(driver, "//div[contains(@id,'table-tasks')]/div/table/thead/tr/th[1]/input", 2).get(0);
		inputFiltroTitulo.sendKeys("tarea2");
		//Ahora Las tareas esperadas son
		String[] template = {"Tarea20","Tarea2"};
		List<String> tareasEsperadas = new ArrayList<String>(Arrays.asList(template));
		//Y las que se muestran en realidad
		//Debemos esperar para que el filtrado acabe.
		Thread.sleep(1000);
		List<WebElement> tareas = CustomEsperaCargaPaginaxpath(driver, "//div[contains(@id,'table-tasks')]/div/table/tbody/tr/td[1]", 2);
		for(int i=0;i<tareas.size();i++){
			assertTrue(tareasEsperadas.contains(tareas.get(i).getText()));
		}
    }
	//PR19: Funcionamiento correcto de la ordenación por categoría.
	@Test
    public void prueba19() {
		loginUser();
		//Nos dirigimos al listado de tareas de hoy
		WebElement botonHoy = CustomEsperaCargaPaginaxpath(driver, "//a[contains(@id,'tareas-hoy')]", 2).get(0);
		botonHoy.click();
		//Ordenamos por categoria
		WebElement botonCategoria = CustomEsperaCargaPaginaxpath(driver, "//table/thead/tr/th[4]", 2).get(0);
		botonCategoria.click();
		
		//Primero salen los que no tienen categoría, Despues los que tienen categoria:
		List<String> tareasSinCategoria = new ArrayList<String>();
		Map<String,String> tareasCategoria = new HashMap<String,String>();
		//las 10 primeras no tienen categoria y siendo de hoy
		for(int i=11;i<=20;i++)
			tareasSinCategoria.add("Tarea"+i);
		//Las siguientes 20 
		for(int i=21;i<=30;i++){
			String categoria="";
			if(i<=23){
				categoria="Category1";
			}
			else if(i<=26){
				categoria="Category2";
			}
			else{
				categoria="Category3";
			}
			tareasCategoria.put("Tarea"+i,categoria);
		}
		//Cogemos los elementos reales
		List<WebElement> Tareas = CustomEsperaCargaPaginaxpath(driver, "//table/tbody/tr/td[1]", 2);
		List<WebElement> categorias = CustomEsperaCargaPaginaxpath(driver, "//table/tbody/tr/td[4]", 2);
		for(int i=0;i<20;i++){
			if(i==8){
				//Debemos pasar a la sigueinte página.
				WebElement page = CustomEsperaCargaPaginaxpath(driver, "//span[@class='ui-paginator-pages']/a[2]", 2).get(0);
				page.click();
				Tareas = CustomEsperaCargaPaginaxpath(driver, "//table/tbody/tr/td[1]", 2);
				categorias = CustomEsperaCargaPaginaxpath(driver, "//table/tbody/tr/td[4]", 2);
			}
			if(i==16){
				//Debemos pasar a la sigueinte página.
				WebElement page = CustomEsperaCargaPaginaxpath(driver, "//span[@class='ui-paginator-pages']/a[3]", 2).get(0);
				page.click();
				Tareas = CustomEsperaCargaPaginaxpath(driver, "//table/tbody/tr/td[1]", 2);
				categorias = CustomEsperaCargaPaginaxpath(driver, "//table/tbody/tr/td[4]", 2);
			}
			if(i<10){
				assertTrue(tareasSinCategoria.contains(Tareas.get(i%8).getText()));
				assertEquals("",categorias.get(i%8).getText()); //Las tareas sin categoría tienen " " como categoría.
			}
			else{
				String categoriaEsperada = tareasCategoria.get(Tareas.get(i%8).getText());
				assertTrue(categoriaEsperada!=null);
				assertEquals(categoriaEsperada,categorias.get(i%8).getText()); //Las tareas sin categoría tienen " " como categoría.
			}
		}
		
    }
	//PR20: Funcionamiento correcto de la ordenación por fecha planeada.
	@Test
    public void prueba20() throws InterruptedException {
		loginUser();
		//Nos dirigimos al listado de tareas de hoy
		WebElement botonHoy = CustomEsperaCargaPaginaxpath(driver, "//a[contains(@id,'tareas-hoy')]", 2).get(0);
		botonHoy.click();
		//ordenamos por fecha
		WebElement botonFechaPlaneada = CustomEsperaCargaPaginaxpath(driver, "//table/thead/tr/th[3]", 2).get(0);
		botonFechaPlaneada.click();
		//Creamos las fechas esperadas
		List<String> fechasEsperadas = new ArrayList<String>();
		for(int i=1;i<=20;i++){
			if(i<=10){
				fechasEsperadas.add(df.format(DateUtil.addDays(DateUtil.today(), i-31)));
			}
			else{
				fechasEsperadas.add(df.format(DateUtil.today()));
			}
		}
		
		//Cogemos los elementos reales
		List<WebElement> fechasPlaneadas = CustomEsperaCargaPaginaxpath(driver, "//table/tbody/tr/td[3]", 2);
		for(int i=0;i<20;i++){
			if(i==8){
				//Debemos pasar a la sigueinte página.
				WebElement page = CustomEsperaCargaPaginaxpath(driver, "//span[@class='ui-paginator-pages']/a[2]", 2).get(0);
				page.click();
				fechasPlaneadas = CustomEsperaCargaPaginaxpath(driver, "//table/tbody/tr/td[3]", 2);
			}
			if(i==16){
				//Debemos pasar a la sigueinte página.
				WebElement page = CustomEsperaCargaPaginaxpath(driver, "//span[@class='ui-paginator-pages']/a[3]", 2).get(0);
				page.click();
				fechasPlaneadas = CustomEsperaCargaPaginaxpath(driver, "//table/tbody/tr/td[3]", 2);
			}
			assertEquals(fechasEsperadas.get(i),fechasPlaneadas.get(i%8).getText());
		}
    }
	//PR21: Comprobar que las tareas que no están en rojo son las de hoy y además las que deben ser.
	@Test
    public void prueba21() throws InterruptedException {
		loginUser();
		
		//Nos dirigimos al listado de tareas de hoy
		WebElement botonHoy = CustomEsperaCargaPaginaxpath(driver, "//a[contains(@id,'tareas-hoy')]", 2).get(0);
		botonHoy.click();
		
		//ordenamos por fecha ya que sabemos a priori que tareas son als que estan retrasadas y evitamos paginar de mas.
		WebElement botonFechaPlaneada = CustomEsperaCargaPaginaxpath(driver, "//table/thead/tr/th[3]", 2).get(0);
		botonFechaPlaneada.click();
		Thread.sleep(500);
		botonFechaPlaneada.click();
		
		//Introducimos las tareas no retrasadas que son las son de hoy 11-20
		Map<String, String> tareasEsperadas = new HashMap<String,String>();
		for(int i=11;i<=20;i++){
			tareasEsperadas.put("Tarea"+i, df.format(DateUtil.today()));
		}
		
		//Primera Página
		List<WebElement> nombresRetrasadas = null; 
		List<WebElement> planeadaRetrasada = null; 
		
		int attempts =0;
		while(attempts <2){
			try{
				nombresRetrasadas = CustomEsperaCargaPaginaxpath(driver, "//div[contains(@id,'table-tasks')]/div/table/tbody/tr[not(contains(@class,'delay'))]/td[1]", 2);
				planeadaRetrasada = CustomEsperaCargaPaginaxpath(driver, "//div[contains(@id,'table-tasks')]/div/table/tbody/tr[not(contains(@class,'delay'))]/td[3]", 2);
			}catch(StaleElementReferenceException e){
				
			}
			attempts++;
		}
		
		for(int i=0;i<nombresRetrasadas.size();i++){
			String fechaPlaneada = tareasEsperadas.get(nombresRetrasadas.get(i).getText());
			assertTrue(fechaPlaneada!=null);
			assertEquals(fechaPlaneada,planeadaRetrasada.get(i).getText());
		}
		
		//Segunda pagina
		WebElement page = CustomEsperaCargaPaginaxpath(driver, "//span[@class='ui-paginator-pages']/a[2]", 2).get(0);
		page.click();
		
		attempts =0;
		while(attempts <2){
			try{
				nombresRetrasadas = CustomEsperaCargaPaginaxpath(driver, "//div[contains(@id,'table-tasks')]/div/table/tbody/tr[not(contains(@class,'delay'))]/td[1]", 2);
				planeadaRetrasada = CustomEsperaCargaPaginaxpath(driver, "//div[contains(@id,'table-tasks')]/div/table/tbody/tr[not(contains(@class,'delay'))]/td[3]", 2);
			}catch(StaleElementReferenceException e){
				
			}
			attempts++;
		}
		
		for(int i=0;i<nombresRetrasadas.size();i++){
			String fechaPlaneada = tareasEsperadas.get(nombresRetrasadas.get(i).getText());
			assertTrue(fechaPlaneada!=null);
			assertEquals(fechaPlaneada,planeadaRetrasada.get(i).getText());
		}
		
    }
	//PR22: Comprobar que las tareas retrasadas están en rojo y son las que deben ser.
	@Test
    public void prueba22() {
		loginUser();
		
		//Nos dirigimos al listado de tareas de hoy
		WebElement botonHoy = CustomEsperaCargaPaginaxpath(driver, "//a[contains(@id,'tareas-hoy')]", 2).get(0);
		botonHoy.click();
		
		//ordenamos por fecha ya que sabemos a priori que tareas son als que no estan retrasadas y evitamos paginar de mas.
		WebElement botonFechaPlaneada = CustomEsperaCargaPaginaxpath(driver, "//table/thead/tr/th[3]", 2).get(0);
		botonFechaPlaneada.click();
		//Introducimos las tareas no retrasadas que son las sonde hoy 1-10
		List<String> tareasEsperadas = new ArrayList<String>();
		for(int i=21;i<=30;i++){
			tareasEsperadas.add("Tarea"+i);
		}
		
		//Primera Página
		List<WebElement> nombresNoRetrasadas = null; 
		
		int attempts =0;
		while(attempts <2){
			try{
				nombresNoRetrasadas = CustomEsperaCargaPaginaxpath(driver, "//div[contains(@id,'table-tasks')]/div/table/tbody/tr[contains(@class,'delay')]/td[1]", 2);
			}catch(StaleElementReferenceException e){
				
			}
			attempts++;
		}
		
		for(int i=0;i<nombresNoRetrasadas.size();i++){
			assertTrue(tareasEsperadas.contains(nombresNoRetrasadas.get(i).getText()));
		}
		
		//Segunda pagina
		WebElement page = CustomEsperaCargaPaginaxpath(driver, "//span[@class='ui-paginator-pages']/a[2]", 2).get(0);
		page.click();
		
		attempts =0;
		while(attempts <2){
			try{
				nombresNoRetrasadas = CustomEsperaCargaPaginaxpath(driver, "//div[contains(@id,'table-tasks')]/div/table/tbody/tr[contains(@class,'delay')]/td[1]", 2);
			}catch(StaleElementReferenceException e){
				
			}
			attempts++;
		}
		
		for(int i=0;i<nombresNoRetrasadas.size();i++){
			assertTrue(tareasEsperadas.contains(nombresNoRetrasadas.get(i).getText()));
		}
    }
	//PR23: Comprobar que las tareas de hoy y futuras no están en rojo y que son las que deben ser.
	@Test
    public void prueba23() throws InterruptedException {
		loginUser();
		
		//Nos dirigimos al listado de tareas de semana
		WebElement botonSemana = CustomEsperaCargaPaginaxpath(driver, "//a[contains(@id,'tareas-semana')]", 2).get(0);
		botonSemana.click();
		
		//ordenamos por fecha ya que sabemos a priori que tareas son als que estan retrasadas y evitamos paginar de mas.
		WebElement botonFechaPlaneada = CustomEsperaCargaPaginaxpath(driver, "//table/thead/tr/th[3]", 2).get(0);
		botonFechaPlaneada.click();
		Thread.sleep(500);
		botonFechaPlaneada.click();
		
		//Sabemos las tareas que no estan retrasadas con su fecha
		Map<String,String> tareasNoRetrasadas = new HashMap<String,String>();
		for(int i=1;i<=20;i++){
			if(i<=10) // del 1-10 son futuras
				tareasNoRetrasadas.put("Tarea"+i,df.format(DateUtil.addDays(DateUtil.today(), 6)));
			else //Las de hoy
				tareasNoRetrasadas.put("Tarea"+i, df.format(DateUtil.today()));
		}
		
		List<WebElement> nombres =null;
		List<WebElement> planeadas=null;
		
		int attempts =0;
		while(attempts <2){
			try{
				nombres = CustomEsperaCargaPaginaxpath(driver, "//div[contains(@id,'table-tasks')]/div/table/tbody/tr[not(contains(@class,'delay'))]/td[1]",2);
				planeadas = CustomEsperaCargaPaginaxpath(driver, "//div[contains(@id,'table-tasks')]/div/table/tbody/tr[not(contains(@class,'delay'))]/td[3]", 2);
			}catch(StaleElementReferenceException e){
				
			}
			attempts++;
		}
		
		//Cogemos todas las tareas
		for(int i=1;i<=20;i++){
			if(i==9){
				//Debemos pasar a la sigueinte página.
				WebElement page = CustomEsperaCargaPaginaxpath(driver, "//span[@class='ui-paginator-pages']/a[2]", 2).get(0);
				page.click();
				attempts =0;
				while(attempts <2){
					try{
						nombres = CustomEsperaCargaPaginaxpath(driver, "//div[contains(@id,'table-tasks')]/div/table/tbody/tr[not(contains(@class,'delay'))]/td[1]",2);
						planeadas = CustomEsperaCargaPaginaxpath(driver, "//div[contains(@id,'table-tasks')]/div/table/tbody/tr[not(contains(@class,'delay'))]/td[3]", 2);
					}catch(StaleElementReferenceException e){
						
					}
					attempts++;
				}
			}
			if(i==17){
				//Debemos pasar a la sigueinte página.
				WebElement page = CustomEsperaCargaPaginaxpath(driver, "//span[@class='ui-paginator-pages']/a[3]", 2).get(0);
				page.click();
				attempts =0;
				while(attempts <2){
					try{
						nombres = CustomEsperaCargaPaginaxpath(driver, "//div[contains(@id,'table-tasks')]/div/table/tbody/tr[not(contains(@class,'delay'))]/td[1]",2);
						planeadas = CustomEsperaCargaPaginaxpath(driver, "//div[contains(@id,'table-tasks')]/div/table/tbody/tr[not(contains(@class,'delay'))]/td[3]", 2);
					}catch(StaleElementReferenceException e){
						
					}
					attempts++;
				}
			}
			
			
			
			if(i<planeadas.size()){ //No sabemos cuantas pueden aparecer. PRECONDICION DE QUE SABEMOS QUE VIENEN ANTES SIEMPRE LAS RETRASADAS!!!
				String planeadaEsperada = tareasNoRetrasadas.get(nombres.get(i%8).getText());
				assertTrue(planeadaEsperada!=null);
				assertEquals(planeadaEsperada,planeadas.get(i%8).getText());
			}
		}
    }
	//PR24: Funcionamiento correcto de la ordenación por día.
	@Test
    public void prueba24() {
		loginUser();
		//Nos dirigimos al listado de tareas de semana
		WebElement botonSemana = CustomEsperaCargaPaginaxpath(driver, "//a[contains(@id,'tareas-semana')]", 2).get(0);
		botonSemana.click();
		//ordenamos por fecha
		WebElement botonFechaPlaneada = CustomEsperaCargaPaginaxpath(driver, "//table/thead/tr/th[3]", 2).get(0);
		botonFechaPlaneada.click();
		
		int rowCount=1;
		for(int i=0;i<30;i++){
			if(i==8){
				//Debemos pasar a la sigueinte página.
				WebElement page = CustomEsperaCargaPaginaxpath(driver, "//span[@class='ui-paginator-pages']/a[2]", 2).get(0);
				page.click();
				rowCount=1;
			}
			if(i==16){
				//Siguiente página
				WebElement page = CustomEsperaCargaPaginaxpath(driver, "//span[@class='ui-paginator-pages']/a[3]", 2).get(0);
				page.click();
				rowCount=1;
			}
			if(i==24){
				//Siguiente página
				WebElement page = CustomEsperaCargaPaginaxpath(driver, "//span[@class='ui-paginator-pages']/a[4]", 2).get(0);
				page.click();
				rowCount=1;
			}
			
			//Cogemos la fecha planeada que corresponde.
			String planeada="";
			int attempts =0;
			while(attempts <2){
				try{
					planeada = CustomEsperaCargaPaginaxpath(driver, "//table/tbody/tr["+rowCount+"]/td[3]", 2).get(0).getText();
				}catch(StaleElementReferenceException e){
					
				}
				attempts++;
			}
			
			
			if(i<=9){
				//Retrasadas categoria.
				assertEquals(df.format(DateUtil.addDays(DateUtil.today(), (i-30))),planeada);
			}
			else if (i<=19){
				//Hoy
				assertEquals(df.format(DateUtil.today()),planeada);
			}
			else{
				//Futuro, 6 dias
				assertEquals(df.format(DateUtil.addDays(DateUtil.today(), 6)),planeada);
			}
			rowCount++;
		}
		
    }
	//PR25: Funcionamiento correcto de la ordenación por nombre.
	@Test
    public void prueba25() {
		loginUser();
		//Nos dirigimos al listado de tareas de semana
		WebElement botonSemana = CustomEsperaCargaPaginaxpath(driver, "//a[contains(@id,'tareas-semana')]", 2).get(0);
		botonSemana.click();
		//ordenamos por fecha
		WebElement botonTitulo= CustomEsperaCargaPaginaxpath(driver, "//table/thead/tr/th[1]/span", 2).get(0);
		botonTitulo.click();
		
		//Las tareas se ordenan por el nombre de modo que-> Tarea1, tarea10,...
		List<String> nombresEsperados = new ArrayList<String>();
		for(int i=1;i<=2;i++){
			nombresEsperados.add("Tarea"+i);
			for(int j=0;j<=9;j++){
				nombresEsperados.add(("Tarea"+i)+j);
			}
		}
		nombresEsperados.add("Tarea3");
		nombresEsperados.add("Tarea30");
		for(int i=4;i<=9;i++)
			nombresEsperados.add("Tarea"+i);
		
		
		int rowCount=1;
		//Cogemos todas las tareas
		for(int i=0;i<30;i++){
			if(i==8){
				//Debemos pasar a la sigueinte página.
				WebElement page = CustomEsperaCargaPaginaxpath(driver, "//span[@class='ui-paginator-pages']/a[2]", 2).get(0);
				page.click();
				rowCount=1;
			}
			if(i==16){
				//Debemos pasar a la sigueinte página.
				WebElement page = CustomEsperaCargaPaginaxpath(driver, "//span[@class='ui-paginator-pages']/a[3]", 2).get(0);
				page.click();
				rowCount=1;
			}
			if(i==24){
				//Debemos pasar a la sigueinte página.
				WebElement page = CustomEsperaCargaPaginaxpath(driver, "//span[@class='ui-paginator-pages']/a[4]", 2).get(0);
				page.click();
				rowCount=1;
			}
			String nombre ="";
			
			int attempts =0;
			while(attempts <2){
				try{
					nombre = CustomEsperaCargaPaginaxpath(driver, "//div[contains(@id,'table-tasks')]/div/table/tbody/tr["+rowCount+"]/td[1]",2).get(0).getText();
				}catch(StaleElementReferenceException e){
					
				}
				attempts++;
			}
			
			assertEquals(nombresEsperados.get(i),nombre);
			rowCount++;
		}
		
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
    public void prueba33() throws InterruptedException {
		loginAdmin();
		
		//Hacemos click en el boton de cerrar sesión.
		WebElement botonCerrarSesión = CustomEsperaCargaPaginaxpath(driver, "//button[contains(@id,'item-cerrarSesion')]", 2).get(0);
		botonCerrarSesión.click();
		
		Thread.sleep(500);
		//Comrpobamos que estamos en la página de login.
		SeleniumUtils.textoPresentePagina(driver, "Autentificación"); 
		
    }
	//PR34: Salir de sesión desde cuenta de usuario normal.
	@Test
    public void prueba34() throws InterruptedException {
		loginUser();
		//Hacemos click en el boton de cerrar sesión.
		WebElement botonCerrarSesión = CustomEsperaCargaPaginaxpath(driver, "//button[contains(@id,'item-cerrarSesion')]", 2).get(0);
		botonCerrarSesión.click();
		
		Thread.sleep(500);
		//Comrpobamos que estamos en la página de login.
		SeleniumUtils.textoPresentePagina(driver, "Autentificación");
    }
	//PR35: Cambio del idioma por defecto a un segundo idioma. (Probar algunas vistas)
	@Test
    public void prueba35() throws InterruptedException {
		SeleniumUtils.ClickSubopcionMenuHover(driver, "form-cabecera:idioma", "form-cabecera:en");
		Thread.sleep(1000);
		SeleniumUtils.textoPresentePagina(driver, "Login");
		SeleniumUtils.textoPresentePagina(driver, "Username");
		SeleniumUtils.textoPresentePagina(driver, "Password");
		SeleniumUtils.textoPresentePagina(driver, "Login");
		SeleniumUtils.textoPresentePagina(driver, "Sign up");
		loginAdmin();
		Thread.sleep(1000);
		SeleniumUtils.textoPresentePagina(driver, "Users list");
		SeleniumUtils.textoPresentePagina(driver, "Restore DB");
		SeleniumUtils.textoPresentePagina(driver, "Login");
		SeleniumUtils.textoPresentePagina(driver, "Email");
		SeleniumUtils.textoPresentePagina(driver, "Administrator");
		SeleniumUtils.textoPresentePagina(driver, "Status");
		WebElement botonCerrarSesión = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//button[contains(@id,'item-cerrarSesion')]", 2).get(0);
		botonCerrarSesión.click();
		loginUser();
		Thread.sleep(1000);
		SeleniumUtils.textoPresentePagina(driver, "Title");
		SeleniumUtils.textoPresentePagina(driver, "Creation date");
		SeleniumUtils.textoPresentePagina(driver, "Planned date");
		SeleniumUtils.textoPresentePagina(driver, "Comments");
		SeleniumUtils.textoPresentePagina(driver, "Tasks list - Inbox");
		SeleniumUtils.textoPresentePagina(driver, "Inbox");
		SeleniumUtils.textoPresentePagina(driver, "Today");
		SeleniumUtils.textoPresentePagina(driver, "This week");
		
    }
	//PR36: Cambio del idioma por defecto a un segundo idioma y vuelta al idioma por defecto. (Probar algunas vistas)
	@Test
    public void prueba36() throws InterruptedException {
		//Probamos si es reversible
		SeleniumUtils.ClickSubopcionMenuHover(driver, "form-cabecera:idioma", "form-cabecera:en");
		SeleniumUtils.ClickSubopcionMenuHover(driver, "form-cabecera:idioma", "form-cabecera:es");
		Thread.sleep(1000);
		SeleniumUtils.textoPresentePagina(driver, "Autentificación");
		SeleniumUtils.textoPresentePagina(driver, "Nombre de usuario");
		SeleniumUtils.textoPresentePagina(driver, "Contraseña");
		SeleniumUtils.textoPresentePagina(driver, "Login");
		SeleniumUtils.textoPresentePagina(driver, "Registrarse");
		loginAdmin();
		Thread.sleep(1000);
		SeleniumUtils.textoPresentePagina(driver, "Listado de usuarios");
		SeleniumUtils.textoPresentePagina(driver, "Restaurar BD");
		SeleniumUtils.textoPresentePagina(driver, "Login");
		SeleniumUtils.textoPresentePagina(driver, "Email");
		SeleniumUtils.textoPresentePagina(driver, "Administrador");
		SeleniumUtils.textoPresentePagina(driver, "Estado");
		WebElement botonCerrarSesión = SeleniumUtils.EsperaCargaPaginaxpath(driver, "//button[contains(@id,'item-cerrarSesion')]", 2).get(0);
		botonCerrarSesión.click();
		loginUser();
		Thread.sleep(1000);
		SeleniumUtils.textoPresentePagina(driver, "Título");
		SeleniumUtils.textoPresentePagina(driver, "Fecha creación");
		SeleniumUtils.textoPresentePagina(driver, "Fecha planeada");
		SeleniumUtils.textoPresentePagina(driver, "Comentarios");
		SeleniumUtils.textoPresentePagina(driver, "Listado de tareas - Inbox");
		SeleniumUtils.textoPresentePagina(driver, "Inbox");
		SeleniumUtils.textoPresentePagina(driver, "Hoy");
		SeleniumUtils.textoPresentePagina(driver, "Semana");
    }
	//PR37: Intento de acceso a un  URL privado de administrador con un usuario autenticado como usuario normal.
	@Test
    public void prueba37() {
		loginUser();
		//Intentamos acceder a una de las paginas de usuario
		driver.get("http://localhost:8280/Notaneitor/admin/users.xhtml");
		//Te tiene que redireccionar a login
		
		SeleniumUtils.textoPresentePagina(driver, "Autentificación");
    }
	//PR38: Intento de acceso a un  URL privado de usuario normal con un usuario no autenticado.
	@Test
    public void prueba38() {
		loginAdmin(); //Nos logeamos como administrador
		
		//Intentamos acceder a una de las paginas de usuario
		driver.get("http://localhost:8280/Notaneitor/user/tasks.xhtml");
		//Te tiene que redireccionar a login
		
		SeleniumUtils.textoPresentePagina(driver, "Autentificación");
		
    }

	



	
	


    
}