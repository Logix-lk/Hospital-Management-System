package com.hospital.management.jdbc;

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

			default:
				listMedicine(request, response);
				break;
			}
			
		}catch(Exception exc) {
			System.out.println("Error is: "+exc);
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
		//listMedicine(request, response);
		
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
