package com.hospital.Pharmaceutical;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.hospital.patient.Patient;


@WebServlet("/PharmaceuticalControllerServlet")
	

public class PharmaceuticalControllerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	//Declaring Object
	private PharmaceuticalDb pdb;
	
	//define connection pool
		@Resource(name="jdbc/web_hospital_db")
		private DataSource datasource;
		
		
		
	//init method
	@Override
		public void init() throws ServletException {
			// Normally in Servlet we use init method instead of constructor in normal class.
			super.init();
			//Initializing object
			//init method is a very good place to initializing the objects. because its execute first.
			try {
				pdb=new PharmaceuticalDb(datasource);
			}catch(Exception ex) {
				throw new ServletException();
			}
			
		}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		try {
			
			//get the instruction 
			String info=request.getParameter("info");
			
			//if instruction is empty
			if(info==null) {
				info="show";
			}
			
			switch (info) {
			
				case "updateForm":
					//this command is for repopulate the form from actual DB values.
					updateMedicine(request,response);
					break;
					
				case "show":
					listMedicine(request, response);
					break;
					
				case "deleteForm":
					deleteMedicine(request,response);
					break;
	
				default:
					listMedicine(request, response);
					break;
			}
			
		}catch(Exception exc) {
			System.out.println("Error is: "+exc);
			//listMedicine(request, response);
		}
		
	}
	
	private void deleteMedicine(HttpServletRequest request, HttpServletResponse response) {
		//Get ID
		String pId=request.getParameter("id");
		//Delete selected medicine from the database
		pdb.deleteMedicineDb(pId);
		//Redirected to the main list
		listMedicine(request, response);
		
		
	}

	private void updateMedicine(HttpServletRequest request, HttpServletResponse response) throws Exception {
		//get ID
		String pId=request.getParameter("id");
		
		//Get related medicine from the database
		Pharmaceutical tmpMed=pdb.selectMed(pId);
		//Place that object in Attribute
		request.setAttribute("tmpMedicine", tmpMed);
		//request Dispatcher
		RequestDispatcher fly2=request.getRequestDispatcher("/updatePharmaceutical.jsp");
		fly2.forward(request, response);
		//Redirected to the main list
		listMedicine(request, response);
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		try {
			
			//get the instruction 
			String infoPost=request.getParameter("infoPost");
			//if instruction is empty
			if(infoPost==null) {
				infoPost="show";
			}
			
			switch (infoPost) {
			
				case "updateDb":
					updateMedicineDb(request,response);
					break;
					
				case "show":
					listMedicine(request, response);
					break;
					
				case "add":
					addMedicine(request,response);
					break;
					
				case "selectPatient":
					selectPatient(request,response);
					break;
					
				case "selectMed":
					selectMedicine(request,response);
					break;
				
				case "updateMed":
					updateBothMedicine(request,response);
					break;
	
				default:
					//listMedicine(request, response);
					break;
			}
			
		}catch(Exception exc) {
			System.out.println("Error is: "+exc);
			exc.printStackTrace();
		}

	}
	
	private void updateBothMedicine(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		//Retrieve all med info from JSP to RESEND Otherwise emptyTable
		String p_id=request.getParameter("pId");
		String p_name=request.getParameter("pName");
		String p_brand_name=request.getParameter("pBrandName");
		String tmpPrice=request.getParameter("pPrice");
		double p_price=Double.parseDouble(tmpPrice);
		
		//Needed Qty
		String tmpNQty=request.getParameter("pQty");
		int nQty=Integer.parseInt(tmpNQty);
		//Available Qty
		String tmpAQty=request.getParameter("availableQty");
		int aQty=Integer.parseInt(tmpAQty);
		
		//Validation
		if(nQty>aQty) {
			
			//Session for show the error in jsp
			request.getSession().setAttribute("validation", "vallerror");
			//create Phrmaceutical object to resends
			Pharmaceutical pTmp=new Pharmaceutical(p_id, p_name, p_brand_name, aQty, p_price);
			request.setAttribute("selectMedDb", pTmp);
			RequestDispatcher fly=request.getRequestDispatcher("/assignMedDb.jsp");
			fly.forward(request, response);
		}else {
			//Retrieving Related User Id from Session
			String patientNic  = (String) request.getSession().getAttribute("medUserId");
			
			//Insert into allocated_medicine table
			pdb.insertAllocatedMeds(patientNic,p_id,nQty);
			
			//Qty for update original pharmaceutical table
			int rQty=aQty-nQty;
			
			//Update the Original Pharmaceutical table
			pdb.updateOriginalMed(p_id,rQty);
			
			//=============================================
			//----Redirect to the select medicine page-----
			//=============================================

			//Retrieve data from database and save in Patient object
				//==From Patient table==
			Patient p1=pdb.selectPatientDb(patientNic);
				//==From allocated_medicine table==
			List<AllocatedPharmaceutical> a1=pdb.showAllocatedMeds(patientNic);
			
			//add object to attributes
				//==Retrieved data From Allocated_medicne==
			request.setAttribute("AllocatedDb", a1);
			
			//send to the Jsp page (view)
			if(p1==null) {
				request.getSession().setAttribute("userdb", "no_user");
				RequestDispatcher fly3=request.getRequestDispatcher("/selectPatient.jsp");
				fly3.forward(request, response);
			}else {
				//==Retrieved data From Patient table== 
				request.setAttribute("PatientDb", p1);
				//==Session for return (otherwise no medicine error will show)
				request.getSession().setAttribute("medDb1", "ret");
				RequestDispatcher fly3=request.getRequestDispatcher("/assignMed.jsp");
				fly3.forward(request, response);
			}	
			//==============================================
			
		}
	}

	private void selectMedicine(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String medId=request.getParameter("pMedId");
		String patientId=request.getParameter("patientId");
		
		//Retrieve Data from DB and save in Pharmaceutical object
		Pharmaceutical pMedDb=pdb.selectMedDb(medId);
		
		//send to the Jsp page (view)
		if(pMedDb==null) {
			
			//=================================================
			//--Validation-- code from the SelectPatient Method
			//=================================================
			Patient p1=pdb.selectPatientDb(patientId);
			//==From allocated_medicine table==
			List<AllocatedPharmaceutical> a1=pdb.showAllocatedMeds(patientId);
		
			//add object to attributes
			//==Retrieved data From Patient table== 
		
			//==Retrieved data From Allocated_medicne==
			request.setAttribute("AllocatedDb", a1);
			
			request.setAttribute("PatientDb", p1);
			request.getSession().setAttribute("medDb1", "NoMed");
			RequestDispatcher fly3=request.getRequestDispatcher("/assignMed.jsp");
			fly3.forward(request, response);
			
		}else {
			//Set Attribute
			request.setAttribute("selectMedDb", pMedDb);
			RequestDispatcher fly4=request.getRequestDispatcher("/assignMedDb.jsp");
			fly4.forward(request, response);
		}
		
	}

	private void selectPatient(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
			String patientId=request.getParameter("pNic");
			 
		
			//Retrieve data from database and save in Patient object
				//==From Patient table==
			Patient p1=pdb.selectPatientDb(patientId);
				//==From allocated_medicine table==
			List<AllocatedPharmaceutical> a1=pdb.showAllocatedMeds(patientId);
			
			//add created object to the attribute
				//==Retrieved data From Patient table== 
			
				//==Retrieved data From Allocated_medicne==
			request.setAttribute("AllocatedDb", a1);
			
			//send to the Jsp page (view)
			if(p1==null) {
				request.getSession().setAttribute("userdb", "no_user");
				RequestDispatcher fly3=request.getRequestDispatcher("/selectPatient.jsp");
				fly3.forward(request, response);
			}else {
				
				request.setAttribute("PatientDb", p1);
				RequestDispatcher fly3=request.getRequestDispatcher("/assignMed.jsp");
				fly3.forward(request, response);
			}	
	}

	private void updateMedicineDb(HttpServletRequest request, HttpServletResponse response) throws Exception {
		//Receive medicines from the JSP form.
			String pId=request.getParameter("phId");
			String pName=request.getParameter("phName");
			String pBrand=request.getParameter("phBrand");
			String tmpprice=request.getParameter("phPrice");
			double pPrice=Double.parseDouble(tmpprice);
			String tmpqty=request.getParameter("phQty");
			int pQty=Integer.parseInt(tmpqty);
		//Create the object
			Pharmaceutical medicineDb=new Pharmaceutical(pId, pName, pBrand, pQty, pPrice);
			
		//Update the Db
			pdb.updateMedicineDb1(medicineDb);
		//Redirect to the main page
			listMedicine(request, response);
	}

	private void addMedicine(HttpServletRequest request, HttpServletResponse response) {
		//Receive medicines from the JSP form.
		String pId=request.getParameter("phId");
		String pName=request.getParameter("phName");
		String pBrand=request.getParameter("phBrand");
		String tmpprice=request.getParameter("phPrice");
		double pPrice=Double.parseDouble(tmpprice);
		String tmpqty=request.getParameter("phQty");
		int pQty=Integer.parseInt(tmpqty);
		
		//Create Medicine Object
		Pharmaceutical medicine=new Pharmaceutical(pId, pName, pBrand, pQty, pPrice);
		
		//Add Medicine object to the DB
		pdb.addPharmaceutical(medicine);
		
		//Redirect to the main page 
		listMedicine(request, response);
		
	}

	//Method
	private void listMedicine(HttpServletRequest request, HttpServletResponse response) {
		
		try {
			//Get Pharmaceutical List from PhatmaceuticalDB class using the object pdb
			List <Pharmaceutical> pList=pdb.getPharmaceutical();
			
			//add list to the request
			request.setAttribute("tmpList", pList);
			
			//send to the Jsp psge (view)
			RequestDispatcher fly=request.getRequestDispatcher("/viewPharmaceutical.jsp");
			fly.forward(request, response);
			
		} catch (Exception e) {		
			e.printStackTrace();
		}	
	}
}
