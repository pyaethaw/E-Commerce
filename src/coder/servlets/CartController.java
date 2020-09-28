package coder.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.google.gson.Gson;

import coder.daos.OrderDAO;
import coder.daos.ProductDAO;
import coder.models.CartItem;
import coder.models.Order;
import coder.models.Product;
import coder.models.User;

@WebServlet("/CartController")
public class CartController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private DataSource ds;

	public CartController() {
		super();
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		try {
			InitialContext initialContext = new InitialContext();
			Context env = (Context) initialContext.lookup("java:comp/env");
			ds = (DataSource) env.lookup("jdbc/TestDB");
		} catch (NamingException e) {
			e.printStackTrace();
		}

	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter pw = response.getWriter();
		ProductDAO pDAO = new ProductDAO();

		Connection con = null;
		try {
			con = ds.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		String param = request.getParameter("action");
		if (param.contentEquals("billout")) {
			billout(request, response, con, pDAO, pw);
		} else if (param.contentEquals("orderout")) {
			orderout(request, response, con, pDAO, pw);
		}

		if (con != null) {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void billout(HttpServletRequest request, HttpServletResponse response, Connection con, ProductDAO pDAO,
			PrintWriter pw) {
		Gson gson = new Gson();
		String items = request.getParameter("items");
		CartItem[] cartItems = gson.fromJson(items, CartItem[].class);
		List<Product> products = new ArrayList<Product>();
		for (CartItem item : cartItems) {
			products.add(pDAO.getSingleProduct(con, item.getId()));
		}
		pw.write(gson.toJson(products));
	}

	private void orderout(HttpServletRequest request, HttpServletResponse response, Connection con, ProductDAO pDAO,
			PrintWriter pw) throws ServletException, IOException {
		String items = request.getParameter("items");
		OrderDAO orderDAO = new OrderDAO();
		User user = (User) request.getSession().getAttribute("user");
		boolean bol = orderDAO.insertNewOrder(con, user.getId(), items);
		if (bol) {
			pw.write("success");
		} else {
			pw.write("error");
		}

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
