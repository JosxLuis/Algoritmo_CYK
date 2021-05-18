import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/*
 * José Luis Aguilar Nucamendi
 * A01174152
 */

public class Algoritmo_CYK_A01174152 {
	
	private Map <Character, ArrayList<String>> gramatica;
	private String cadena;
		
	public Algoritmo_CYK_A01174152(String[] producciones, String cadena) {
		this.cadena = cadena;
		this.gramatica = convertirHash(producciones);
	}
	

	public Map <Character, ArrayList<String>> convertirHash(String[] producciones){
		if(producciones == null) {
			return null;
		}		
		Map <Character, ArrayList<String>> ordenar = new Hashtable<>();
		for(String caracter: producciones) {
			if(!ordenar.containsKey(caracter.charAt(0))) {
				ordenar.put(caracter.charAt(0),new ArrayList(Arrays.asList(caracter.substring(3).split("\\|"))));
			}
		}
		System.out.println(ordenar);
		return ordenar;
	} 
	
	
	public void Chomsky() {
		if(verificar(this.gramatica)) {
			System.out.println("Gramatica está en forma normal de Chomsky");
			if(CYK(gramatica, cadena)) {
				System.out.println("Pertenece");
			}else {
				System.out.println("No pertenece");
			}
		}else {
			System.out.println("Gramatica no está en forma normal de Chomsky, pero lo vamos a convertir");	
			eliminarEpsilon(this.gramatica); 
			eliminarUnitarias(this.gramatica);
			eliminarInutiles(this.gramatica);
			sustitucionTerminales(this.gramatica);
			sustitucionNoTerminales(this.gramatica);
			System.out.print(gramatica);
			
			if(CYK(gramatica, cadena)) {
				System.out.println("Pertecene");
			}else {
				System.out.println("No pertenece");
			}
		}
	}

	public boolean verificar(Map <Character, ArrayList<String>> gramatica) {
		//recorró el hasmap
		for(Map.Entry<Character, ArrayList<String>> entry: gramatica.entrySet()) {
			System.out.println("----");
			System.out.println("clave=" + entry.getKey() + ", valor=" + entry.getValue());
			ArrayList<String> proGramatica = entry.getValue();
			for(String simbolos : proGramatica) {
				System.out.print(simbolos+",");
				// Los que son de 2 ya estan en la forma normal de chomsky
				if(simbolos.length() >= 3) { // Para cada producción que tenga màs de 3, hay que hacer el algoritmo
					return false;
				}
				
				int cantNoTerminales = 0;
				
				for(int i=0; i<simbolos.length(); i++) {
					
					// Rango caracteres mayusculas A = 65, Z = 90.
					if(simbolos.charAt(i)>=65 && simbolos.charAt(i)<=90) { //Mayusculas
						cantNoTerminales++; 
					}
					
					if(simbolos.charAt(i)=='e') {
						return false;
					}
					
					// range letras minusculas a = 97, z = 122.
					
					// Todo terminal está solo
					if(simbolos.charAt(i)>97 && simbolos.length()>1) {
						return false;
					}
					
					// Si tiene producciones unitarias y más de dos NoTerminales
					if(cantNoTerminales > 2 || cantNoTerminales == 1 && simbolos.length() == 1) {
						return false;
					}
					
					//producciones inutiles
					if(simbolos.charAt(0) == entry.getKey().charValue()) {
						return false;
					}
				}
			}
		}
		
		return true;
	}
	
	private boolean CYK(Map<Character, ArrayList<String>> gramaticas, String cadena) {
		ArrayList<ArrayList<ArrayList<Character>>> table = new  ArrayList<ArrayList<ArrayList<Character>>>();
		int num = cadena.length();
		for(int i=0;i<cadena.length()+1;i++) {//row
			ArrayList<ArrayList<Character>> columna =  new ArrayList<>();
			for(int j =0; j<num; j++ ) {//cadena
				
				
				if(i==0) {//Poner la cadena en el array 0
					ArrayList<Character> simbolos =  new ArrayList<>(); 
					simbolos.add(cadena.charAt(j));
					columna.add(simbolos);
				}else if(i==1){ //Primer Nivel asi que solo tendran un no Terminal
					ArrayList<Character> simbolosNuevos =  new ArrayList<>();
					for(Map.Entry<Character, ArrayList<String>> entry : gramaticas.entrySet()) {
						ArrayList<String> producciones = entry.getValue(); 
						
						//Contador de caracteres en casillas anteriores
						String produccionTerminal = producciones.get(0);
						Character casillaAnterior = table.get(i-1).get(j).get(0);
						if(produccionTerminal.charAt(0)==casillaAnterior) {
							simbolosNuevos.add(entry.getKey());
							columna.add(simbolosNuevos);
							break;
						}
					}if(simbolosNuevos.isEmpty()) {
						return false;
					}
				}else {
					int m = 1; //Row de primera columna
					int n = i-1;//Row de columna en movimiento
					int x = j+1; //Avanza de columna
					ArrayList<Character> SimbolosNuevos = new ArrayList<>();
					boolean produccionEncontrada = false;
					
					while(m<i) {
						//Primero es iteracion de las dos columnas para ver sus combinaciones
						ArrayList<Character> column1 = table.get(m).get(j);
						ArrayList<Character> column2 = table.get(n).get(x);
						
						//Iteracion de las dos ArrayList
						for(Character c1 : column1) {
							for(Character c2: column2 ) {
								if(c1 !=null && c2!=null) {
									String combinacion = ""+c1+c2;
									
									//Iteración de gramaticas para saber si alguien produce la combinacion
									for(Map.Entry<Character, ArrayList<String>> entry :gramaticas.entrySet()) {
										ArrayList<String> noTerminal = entry.getValue();
										for(String produccion: noTerminal) {
											if(produccion.equals(combinacion)) {
												SimbolosNuevos.add(entry.getKey());
												produccionEncontrada = true;
											}
										}
									}
								}
							}
						}
						//Avance
						m++; n--; x++;
					}
					if(!produccionEncontrada) {
						SimbolosNuevos.add(null);
					}
					columna.add(SimbolosNuevos);
				}
					
			}table.add(columna);
			if(i>0)
				num--;
		}
		
		ArrayList<Character> VerificacionSimboloFinal = table.get(cadena.length()).get(0);
		
		for(Character c : VerificacionSimboloFinal) {
			if(c == null)
				return false;
			
			if(c == 'S') {
				Node<Character> node =  new Node<Character>('S');
				DerivationTree<Node<Character>> tree = new DerivationTree(node);
				arbolDerivacion(tree.root, table, table.size()-1, 0);
				System.out.println("Nivel 0: "+tree.root.getInfo());
				imprimirArbol(tree.root.left,tree.root.right,1);
				return true;
				
			} 
		}
		
		return false;
	}
	
	public void arbolDerivacion(Node root, ArrayList<ArrayList<ArrayList<Character>>> table, int row, int column) {
		
		Node<Character> tmp = root;
		Node<Character> tmp2 = root;
		root.setInfo(table.get(row).get(column).get(0));
		int terminales = 0;
		int indexR = row;
		int indexC =column;
		
		while(terminales<cadena.length()) {
			while(indexR>0) {
			    int	indexRtmp = indexR-1;
			    int indexCtmp = indexC+1;
			    if(tmp!=null) {
			    	tmp2 = tmp;
				    while(indexRtmp>0) {
						if(table.get(indexRtmp).get(indexCtmp).get(0)!=null) {
							tmp.right = new Node<Character>(table.get(indexRtmp).get(indexCtmp).get(0));
							tmp = tmp.getRight();
						}indexRtmp--;
						indexCtmp++;
				    }indexRtmp=indexR-1;
				    indexCtmp = indexC;
				    while(indexRtmp>0) {
				    	if(table.get(indexRtmp).get(indexCtmp).get(0)!=null) {
					    	tmp = tmp2;
					    	tmp.left = new Node<Character>(table.get(indexRtmp).get(indexCtmp).get(0));
					    	tmp = tmp.left;
					    	int fila =indexRtmp-1;
					    	int columna = indexCtmp+1;
					    	Node tmpRight = tmp;
					    	while(fila>0) {
					    		tmp2 = tmp; 
								if(table.get(fila).get(columna).get(0)!=null) {
									tmpRight.right = new Node<Character>(table.get(fila).get(columna).get(0));
									tmpRight = tmpRight.getRight();
								}fila--;
								columna++;
							 }
				    	}indexRtmp--;
				    }
			    }indexR--;
			    indexC++;
			    tmp = tmp2.right;
			    terminales++;
			}
		}
	}

	public void imprimirArbol(Node left, Node right, int nivel) {
		if(left!=null) {
			System.out.print("Nivel "+nivel+": "+left.getInfo()+", ");
			imprimirArbol(left.left, left.right, nivel+1);
		}
		
		if(right!=null) {
			System.out.print(right.getInfo()+"\n");
			imprimirArbol(right.left, right.right, nivel+1);
		}
		
	}
	
	
	// FNC
	// 1. Eliminar producciones epsilom
	// 2. Eliminar producciones unitarias
	// 3. Eliminar simbolos inútiles
	
	public void eliminarEpsilon(Map<Character, ArrayList<String>> gramatica) {
		ArrayList<Character> reemplazables = new ArrayList<>(); 
		for(Map.Entry<Character, ArrayList<String>> entry : gramatica.entrySet()) {
			boolean cadenaVacia = false;
			ArrayList<String> producciones=entry.getValue();
			for(String producto : producciones) {
				if(producto.charAt(0)=='e') {
					reemplazables.add(entry.getKey());
					producciones.remove(producto);
					cadenaVacia = !cadenaVacia;
				}
				if(cadenaVacia == true){
					break;
				}
			}
		}
		
		// Agregación de la nueva producción
		for(Map.Entry<Character, ArrayList<String>> entry : gramatica.entrySet()) {
			ArrayList<String> producciones = entry.getValue();
			ArrayList<String> nuevaProduccion = new ArrayList<>();
			boolean bandera = false;
			for(String producto : producciones) {
				StringBuilder nuevoProducto = new StringBuilder();
				for(int i=0;i<producto.length();i++) {
					if(!reemplazables.contains(producto.charAt(i))) {
						nuevoProducto.append(producto.charAt(i));
					}else {
						bandera = true;
					}
				}if(bandera == true) {
					nuevaProduccion.add(nuevoProducto.toString());
					bandera = false;
				}
			}for(String simbolos: nuevaProduccion) {
				producciones.add(simbolos);
			}
		}
		
	}
	
	public void eliminarUnitarias (Map<Character, ArrayList<String>> gramatica){
		for(Map.Entry<Character, ArrayList<String>> entry : gramatica.entrySet()) {
			ArrayList<String> prodReemplazo = new ArrayList<>();
			ArrayList<String> prodGramatica = entry.getValue();
			for(String producto : prodGramatica) {
				// Rango caracteres mayusculas A = 65, Z = 90.
				if(producto.charAt(0)>=65 && producto.charAt(0)<=90 && producto.length()==1) {
					ArrayList<String> reemplazador = gramatica.get(producto.charAt(0));
					prodGramatica.remove(producto);
					prodReemplazo.addAll(reemplazador);
				}
			}prodReemplazo.addAll(prodGramatica);
			this.gramatica.put(entry.getKey(), prodReemplazo);
		}
	}
	
	public void eliminarInutiles (Map<Character, ArrayList<String>> gramatica) {
		ArrayList<Character> inutiles = new ArrayList<>();
		for(Map.Entry<Character, ArrayList<String>> entry : gramatica.entrySet()) {
			ArrayList<String> producciones = entry.getValue();
			for(String producto : producciones) {
				if((producto.charAt(0) == entry.getKey().charValue() && producto.length()==1) || (producto.charAt(0) == entry.getKey().charValue() && producto.charAt(1) == entry.getKey().charValue())){
					inutiles.add(entry.getKey());
				}
			}
		}
		
		for(Character caracter : inutiles) {
			gramatica.remove(caracter);
		}
		
		
		for(Map.Entry<Character, ArrayList<String>> entry : gramatica.entrySet()) {
			ArrayList<String> producciones = entry.getValue();
			ArrayList<String> produccionesUtiles = new ArrayList<>(); 
			boolean remove = false;
			if(inutiles.contains(entry.getKey())) {
				remove = true;
			}else {
				for(String producto : producciones) {
					boolean util = true; 
					for(int i = 0;i<producto.length();i++) {
						if(inutiles.contains(producto.charAt(i))){
							util = false;
						}
					}
					
					if(util) {
						produccionesUtiles.add(producto);
					}
				}
			}
				gramatica.put(entry.getKey(), produccionesUtiles);
		}
		
		Set<Character> simbolos = new HashSet<>(gramatica.keySet());
		for(Map.Entry<Character, ArrayList<String>> entry : gramatica.entrySet()) {
			ArrayList<String> producciones = entry.getValue();
			for(String producto : producciones) {
				 for(int  i = 0; i<producto.length();i++) {
					 if(simbolos.contains(producto.charAt(i))){
						 simbolos.remove(producto.charAt(i));
					 }
				}
			}
		}
		
		for (Character simbolo : simbolos) {
	        gramatica.remove(simbolo);
	     }
	}
	
	public void sustitucionTerminales(Map<Character, ArrayList<String>> gramaticas) {
		Set<Character> produccionesAnteriores = new HashSet<>();
		
		int simbolsBegin = 65;
		
		for(Map.Entry<Character, ArrayList<String>> entry : gramaticas.entrySet()) {
			if(!produccionesAnteriores.contains(entry.getKey())) {
				produccionesAnteriores.add(entry.getKey());
			}
		}
		
		//Nuevas Producciones
		Map<Character, Character> terminalesProducciones = new Hashtable<>();
		Map<Character, ArrayList<String>> nuevasPro = new Hashtable<>();
		
		//Sustitucion de terminales 
		for(Map.Entry<Character, ArrayList<String>> entry : gramaticas.entrySet()) {
			ArrayList<String> producciones = entry.getValue();
			ArrayList<String> reemplazoATerminales = new ArrayList<>();
			for(String produccion: producciones) {
				for(int i=0;i<produccion.length();i++) {
					if(produccion.charAt(i)>=97) {//terminales
						char c = (char)simbolsBegin;
						if(!terminalesProducciones.containsKey(produccion.charAt(i))) {
							while(nuevasPro.containsKey(c) || produccionesAnteriores.contains(c)) {//Encontrar letra no terminal
								simbolsBegin++;
								c = (char)simbolsBegin;	
							}
							terminalesProducciones.put(produccion.charAt(i), c);
							ArrayList<String> terminal = new ArrayList<>();
							terminal.add(produccion.charAt(i)+"");
							nuevasPro.put(c, terminal);
							produccion = produccion.replace(produccion.charAt(i),c);
						}else{
							produccion = produccion.replace(produccion.charAt(i),terminalesProducciones.get(produccion.charAt(i))); 
						}
					}
				}reemplazoATerminales.add(produccion);
			}gramaticas.put(entry.getKey(), reemplazoATerminales);
		}gramaticas.putAll(nuevasPro);
	}
	
	private void sustitucionNoTerminales(Map<Character, ArrayList<String>> gramaticas) {
		Map<Character, ArrayList<String>> produccionesNoTerminales =  new Hashtable<>();
		int simbolos = 65; 
		
		for (Map.Entry<Character, ArrayList<String>> entry : gramaticas.entrySet()) {
			ArrayList<String> producciones = entry.getValue();;
			ArrayList<String> nuevaProduccion = new ArrayList<String>();
			for(String producto : producciones) {
				int numNoTerminales= 0;
				boolean flag = false;
				for(int i=producto.length()-1;i>=0;i--) {
					if(flag) {
						i=producto.length()-1;
						flag = false;
					}
					if(producto.charAt(i)>64 && producto.charAt(i)<91) {
						numNoTerminales++;
					}
					
					if(numNoTerminales==2 && (i-1)>=0) {
						char c = (char)simbolos;
						if(produccionesNoTerminales.containsValue(producto.substring(i, i+2))) {
							boolean encontrado=false;
							 for(Map.Entry<Character, ArrayList<String>> iterator : produccionesNoTerminales.entrySet()) {
								 if(encontrado)
									 break;
								 ArrayList<String> lista = entry.getValue();
								  for(String str : lista) {
									  if(str.equals(producto.substring(i, i+2)));
									  encontrado = true;
								  }
							  }
							 
						}else {
							 c = (char)simbolos;
							while(gramaticas.containsKey(c) || produccionesNoTerminales.containsKey(c)) {
								simbolos++;
								c = (char)simbolos;
							}
						}
						flag = true;
						ArrayList<String> str = new ArrayList<>(); //Produccion de dos terminales
						str.add(producto.substring(i, i+2));
						produccionesNoTerminales.put(c, str);
						numNoTerminales=0;
						producto = producto.substring(0, i)+c;
					}
				}nuevaProduccion.add(producto);
			}gramaticas.put(entry.getKey(), nuevaProduccion);
		}gramaticas.putAll(produccionesNoTerminales);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	
		String[] producciones = {"S->aSb|e"};
		String cadena = "aaabbb";
		Algoritmo_CYK_A01174152 cyk = new Algoritmo_CYK_A01174152(producciones, cadena);
		cyk.Chomsky();
	}
	
	private class DerivationTree<E>{
		private Node<E> root;
		
		public DerivationTree() {
			this.root = null;
		}
		
		public DerivationTree(Node<E> node) {
			this.root = node;
		}

		public Node<E> getRoot() {
			return root;
		}

		public void setRoot(Node<E> root) {
			this.root = root;
		}
	}

	private class Node<E> {
		 private E info;
		 private Node<E> left;
		 private Node<E> right;
			
		 public Node(E info) {
			 this.info = info;
			 this.left = null;
			 this.right = null;
		 }

		public E getInfo() {
			return info;
		}

		public void setInfo(E info) {
			this.info = info;
		}

		public Node<E> getLeft() {
			return left;
		}

		public void setLeft(Node<E> left) {
			this.left = left;
		}

		public Node<E> getRight() {
			return right;
		}
		
		public void setRight(Node<E> right) {
			this.right = right;
		}
	}
}
