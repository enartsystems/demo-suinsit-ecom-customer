package com.suinsit.webapp.nocode;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.enartframework.core.shared.commons.SortCollections;
import org.enartframework.core.shared.commons.TextParser;
import org.enartframework.core.shared.logger.EnartLoggerFactory;
import org.enartframework.core.shared.logger.IEnartLogger;
import org.enartframework.nocode.dao.EntityDao;
import org.enartframework.nocode.dao.IEntityLocal;
import org.enartframework.nocode.datamodel.jdbc.Criteria;
import org.enartframework.nocode.datamodel.jdbc.Criterias;
import org.enartframework.nocode.datamodel.jdbc.Evaluation;
import org.enartframework.nocode.datamodel.jdbc.Operation;
import org.enartframework.nocode.datamodel.model.Entity;
import org.enartframework.nocode.datamodel.model.Field;
import org.enartframework.nocode.datamodel.model.Query;
import org.enartframework.nocode.datamodel.model.QueryBuilder;
import org.enartframework.orm.DataScroll;
import org.enartframework.security.Usuario;
import org.enartframework.suinsit.Context;
import org.enartframework.suinsit.factory.IFactoryArchitect;
import org.enartframework.web.exception.UiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.suinsit.nocode.web.BuildEntityFromClass;
import org.suinsit.nocode.web.MenuBean;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkmax.zul.Navbar;
import org.zkoss.zul.Div;
import org.zkoss.zul.Style;

import com.enartsystems.mapping.BuilderModel;
import com.enartsystems.platform.nocode.builder.ScreenModel;
import com.enartsystems.webapp.beans.AplicationBean;
import com.enartsystems.webapp.front.zkoss.MasterHandler;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;


@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
@Slf4j
public class DashboardUI extends MasterHandler{
	private static final long serialVersionUID = 1L;
	ObjectMapper mapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);;
	@WireVariable("ctxBean")
	protected Context ctxBean;
	@WireVariable("entityDao")
	IEntityLocal dao;
	@WireVariable("context")
	GenericApplicationContext context;
	@WireVariable
	private Environment environment;
	@WireVariable
	@Autowired
	DataSource datasource;
	DataSource ds;
	String pathDirScreen;
	String pathDirModel;
	String pathDirSchema;
	String pathMenu;
	String appName;
	String pageName;
	AplicationBean proyect;
	@Wire
	Style theme;
	Entity Ssoruserapp;
	Class<?> clsSsoruserapp;
	Entity eUsuario;
	Class<?> clsUsuario;
	BuilderModel builder;
	Query query;
	ScreenModel screenModel;
	List<Entity> beans = new ArrayList<>();
	Criterias criterias = new Criterias();
	Properties lang;
	String memory;
	EventQueue eventLocale;
	Component view;
	List<MenuBean> menus = new ArrayList<>();
	Entity permisos;
	String menu;
	Class<?> clsPermisos;
	
	List<AplicationBean> applications = new ArrayList<AplicationBean>();
	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getPageName() {
		return pageName;
	}

	public void setPageName(String pageName) {
		this.pageName = pageName;
	}
	public String getMenu() {
		return menu;
	}
	public void setMenu(String menu) {
		this.menu = menu;
	}
	public List<MenuBean> getMenus() {
		return menus;
	}
	public void setMenus(List<MenuBean> menus) {
		this.menus = menus;
	}
	
	public AplicationBean getProyect() {
		return proyect;
	}
	public void setProyect(AplicationBean proyect) {
		this.proyect = proyect;
	}
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws Exception {
		Selectors.wireComponents(view, this, false);
		Selectors.wireVariables(page, this, Selectors.newVariableResolvers(getClass(), null));
		this.view = view;
		pathDirModel =  ctxBean.getPathHome() + File.separator + "data" + File.separator +"model";
		URLClassLoader urlClassLoader;
		  ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try {
			urlClassLoader = new URLClassLoader(new URL[]{new File(ctxBean.getPathClasses()).toURL()},
					 classLoader);
			Thread.currentThread().setContextClassLoader(urlClassLoader);
		} catch (MalformedURLException e) {
			log.error(e.getMessage());
		}
		builder = new BuilderModel(ctxBean.getPathHome()+ File.separator + "data"+ File.separator + "model");
		clsSsoruserapp = Thread.currentThread().getContextClassLoader().loadClass("org.suinsit.apps.admin.Ssoruserapp");
		clsUsuario = Thread.currentThread().getContextClassLoader().loadClass("org.suinsit.apps.admin.Ssousuario");
		clsPermisos = Thread.currentThread().getContextClassLoader().loadClass("org.suinsit.apps.admin.Ssovpermrol");
		Ssoruserapp = BuildEntityFromClass.build(clsSsoruserapp);	
		eUsuario = BuildEntityFromClass.build(clsUsuario);
		permisos = BuildEntityFromClass.build(clsPermisos);  
		
		Ssoruserapp = builder.loadFromDataGrid("SSORUSERAPP");
		clsSsoruserapp = Thread.currentThread().getContextClassLoader().loadClass("org.suinsit.apps."+Ssoruserapp.getNamespace()+"."+TextParser.getClassNameFormater(Ssoruserapp.getCoentity()));
		eUsuario = builder.loadFromDataGrid("SSOUSUARIO");
		clsUsuario = Thread.currentThread().getContextClassLoader().loadClass("org.suinsit.apps."+eUsuario.getNamespace()+"."+TextParser.getClassNameFormater(eUsuario.getCoentity()));
		permisos = builder.loadFromDataGrid("SSOVPERMROL");
		clsPermisos = Thread.currentThread().getContextClassLoader().loadClass("org.suinsit.apps."+permisos.getNamespace()+"."+TextParser.getClassNameFormater(permisos.getCoentity()));
		
//		clsSsomenuitem = Thread.currentThread().getContextClassLoader().loadClass("org.suinsit.apps.admin.Ssomenuitem");
//		clsBpmnvprocessemails= Thread.currentThread().getContextClassLoader().loadClass("org.suinsit.apps.bpmn.Bpmnvprocessemails");
//		Bpmnvprocessemails= BuildEntityFromClass.build(clsBpmnvprocessemails);
//		Ssomenuitem =  BuildEntityFromClass.build(clsSsomenuitem);
		
//		Crmrempuser = builder.loadFromDataGrid("CRMREMPUSER");
//		clsCrmrempuser= Thread.currentThread().getContextClassLoader().loadClass("org.suinsit.apps."+Crmrempuser.getNamespace()+"."+TextParser.getClassNameFormater(Crmrempuser.getCoentity()));

		ds = environment.getProperty("APPLICATION_DS", DataSource.class);
		initDao();
		eventLocale= EventQueues.lookup("locale",EventQueues.SESSION,true);
		loadApps(getUser());
	}
	
	private void loadApps(Usuario usuario) {
    	List<String> lfields = new ArrayList<>();
     	Ssoruserapp.getFields().forEach(sfield->{
			 lfields.add(sfield.getAtribute());
		});
		query = QueryBuilder.buildQueryWithLeftJoin(Ssoruserapp, List.of(Ssoruserapp.getForeingByAlias("idssoaplicacion")),lfields.toArray(new String[lfields.size()])); 
		criterias = new Criterias();
		Field iduser = eUsuario.copy().getFieldByAtribute("IDXSSOUSUARIO").copy();
		iduser.setField("IDSSOUSUARIO0");
		iduser.setAtribute("IDSSOUSUARIO0");
		iduser.setValue(usuario.getId());	
		criterias.addCriteria(new Criteria(Operation.AND, Evaluation.EQUALS, iduser));
		try {
				listProjects(dao.query(clsSsoruserapp,new DataScroll(), query, criterias).getDataList());
		} catch (Exception e) {
				log.error(e.getMessage());
		}

    }
	
	private void listProjects(List<Object> apps) throws Exception, JsonMappingException, IOException {
		String path = ctxBean.getPathHome()+File.separator+"apps";
		 File fil = new File(path);
			fil.mkdirs();
			for(File file :fil.listFiles()) {
				if(file.isDirectory()) {
					File[] fils = file.listFiles(new FileFilter() {
						@Override
						public boolean accept(File f) {
							if (f.isFile()) { 
								if(f.getName().endsWith("json") && f.getName().startsWith("applica")) {
									return true;	
								}
							}
							return false;
						}
					});
					for (File file1 : fils) {
						AplicationBean ab = mapper.readValue(file1, AplicationBean.class); 
						boolean add=false;
						for(Object ap:apps){
							String prefix = (String) getNested(ap, "idssoaplicacion.namespace");
							if(ab.getProjectBase().getNamespace().equalsIgnoreCase(prefix)) {
								ab.getProjectBase().setIdxproject((Long)getNested(ap, "idssoaplicacion.idxaplicacion"));
								ab.getProjectBase().setIcono((String) getNested(ap, "idssoaplicacion.iconclass"));
								ab.getProjectBase().setTitle((String) getNested(ap, "idssoaplicacion.titulo"));
								add=true;
								break;
							}
						};
						//prefijo
						if(add) {
							String pathMenu = ctxBean.getPathHome() + File.separator + "apps" + File.separator + TextParser.getFormaterUnixPath(ab.getProjectBase().getNamespace()).toLowerCase() + File.separator +"webapp"+ File.separator+"menu"; 
							applications.add(ab);
							//loadMenu(pathMenu,ab.getProjectBase().getNamespace());
						}
					}
				}
			}
			  new SortCollections().sort(applications, "projectBase.txname", true);
			  loadMenusPerms();
	}
    private void loadMenusPerms() throws Exception, JsonMappingException, IOException {
    	Field r = permisos.getFieldByAtribute("ROL").copy();
		try {
			r.setValue((List<String>)getUser().getRoles());
		} catch (UiException e1) {
			log.error(e1.getMessage());
		}
		List<String> lfields = new ArrayList<>();
		permisos.getFields().forEach(f->{
			lfields.add(f.getAtribute());
		});
		QueryBuilder.buildQueryWithoutJoin(permisos, lfields.toArray(new String[lfields.size()]));
		applications.forEach(ab->{
			Field a = permisos.getFieldByAtribute("IDXAPLICACION").copy();
			a.setValue(ab.getProjectBase().getIdxproject());
			criterias = new Criterias();
			criterias.addCriteria(new Criteria(Operation.AND,Evaluation.IN,r));
			criterias.addCriteria(new Criteria(Operation.AND,Evaluation.EQUALS,a));	
			DataScroll ds = new DataScroll();
			ds.setMaxRows(100);
			try {
				addMenus(ab,dao.query(clsPermisos,ds, QueryBuilder.buildQueryWithoutJoin(permisos, lfields.toArray(new String[lfields.size()])),  criterias).getDataList());
				Executions.getCurrent().getSession().getAttributes().put("APP", ab);
			} catch (Exception e1) {
				log.error(e1.getMessage());
			}
		});
	}
    private void addMenus(AplicationBean ab,List<Object>perms) throws Exception {
    	if(perms.size()==0)return;
    	new SortCollections().sort(perms, "menu", true);
    	String smenu="";
    	MenuBean appMenu = new MenuBean();
    	appMenu.setName(ab.getProjectBase().getTitle());
    	appMenu.setIconClass(ab.getProjectBase().getIcono());
    	appMenu.setItems(new ArrayList<>());
    	MenuBean menuBean =null;
    	List<String> items = new ArrayList<>();
    	for(Object perm:perms) {
    		String menu = (String) getNested(perm, "menu");
    		if(!menu.equals(smenu)) {
    			smenu =menu;
    			if(menuBean==null) {
    				menuBean = new MenuBean();
    				menuBean.setItems(new ArrayList<>());
    			}else {
    				appMenu.getItems().add(menuBean);
    				menuBean = new MenuBean();
    				menuBean.setItems(new ArrayList<>());
    			}
    			menuBean.setIconClass((String) getNested(perm, "icono"));
				menuBean.setName(smenu);
    		}
    		if(!items.contains((String) getNested(perm, "item"))) {
    			items.add((String) getNested(perm, "item"));
    			MenuBean item = new MenuBean();
        		item.setName((String) getNested(perm, "item"));
        		item.setNamespace((String) getNested(perm, "namespace"));
        		item.setPageUrl((String) getNested(perm, "page"));
        		menuBean.getItems().add(item);
    		}
    		
    	}
    	appMenu.getItems().add(menuBean);
    	menus.add(appMenu);
    };
	private void initDao() {
		if (dao == null) {
			dao = new EntityDao();
			((EntityDao) dao).setDataSource(ds);
		}
	}
	@Wire
	Navbar navbar2;
	@Wire
	Div iddesktop;
	
	@Command("selectItem")
	public void onSelectItem(@BindingParam("item")MenuBean item,@BindingParam("main")String menu) {
		Map<String,Object> args = new HashMap();
		 args.put("PATH_MODEL", pathDirModel);
		 String	pathDirScreen = ctxBean.getPathHome() + File.separator + "apps" + File.separator + TextParser.getFormaterUnixPath(item.getNamespace()).toLowerCase() + File.separator +"webapp"+ File.separator+"pages";
		 String	pathDirSchema = ctxBean.getPathHome() + File.separator + "apps" + File.separator + TextParser.getFormaterUnixPath(item.getNamespace()).toLowerCase() + File.separator +"webapp"+ File.separator+"screens";
		 args.put("PATH_SCREEN", pathDirScreen);
		 args.put("PATH_SCHEMA", pathDirSchema);
		 applications.forEach(ap->{
			 if(ap.getProjectBase().getNamespace().equals(item.getNamespace())) {
				 setApplication(ap);
				 		 
			 }
		 });
		 
		 this.menu=menu;
		 if(item.getPageUrl()!=null) {
			 appendPage(pathDirScreen + File.separator +item.getPageUrl(),iddesktop,args);
		 }else {
			 String prefix = item.getPrefix()!=null?item.getPrefix():"index";
			 appendPage(pathDirScreen + File.separator +prefix+item.getNameScreen()+".zul",iddesktop,args);	 
		 }
		 	
	}
	
	@Override
	protected void doAfterCompose() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IFactoryArchitect getFactoryByProject() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void save() {
		// TODO Auto-generated method stub
		
	}


}
