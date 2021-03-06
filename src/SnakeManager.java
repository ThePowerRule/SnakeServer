import java.util.ArrayList;

public class SnakeManager {
	private volatile ArrayList<ClientBridge> clients = new ArrayList<ClientBridge>();
	private Dispatcher dispatcher;
	public SnakeManager() {
		dispatcher = new Dispatcher(this);
		dispatcher.start();
	}

	/**
	 * Adds a ClientBridge instance to the list of clients
	 * @param b
	 */
	public synchronized void addClientBridge(ClientBridge b){
		clients.add(b);
		System.out.println("Clients total: " + clients.size());
	}

	/**
	 * @return the clients
	 */
	public synchronized ArrayList<ClientBridge> getClients() {
		return clients;
	}

	/**
	 * @param i - the index of the Snake instance requested
	 * @return the Snake if one exists
	 */
	public synchronized Snake getSnakeAt(int i){
		return clients.get(i).getSnake();
	}

	/**
	 * @param i - the index of the ClientBridge instance requested
	 * @return the ClientBridge if one exists
	 */
	public synchronized ClientBridge getBridgeAt(int i){
		return clients.get(i);
	}

	/**
	 * Sends a message to all of the applications connected to the server
	 * @param msg - the message to send to all of the applications 
	 * connected to the server
	 */
	private void spam(int msg){
		for(ClientBridge bridge:clients){
			if(bridge.isLive()){
				bridge.getOutStream().println(msg);
			}
		}
	}

	/**
	 * Closes the connections to all of the client applications
	 */
	public synchronized void close(){
		spam(ClientBridge.CLOSE);
		spam(ClientBridge.END);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		close();
		for(ClientBridge bridge:clients){
			bridge.closeConnection();
		}
	}

	/**
	 * Updates all of the snakes
	 */
	public synchronized void updateAllSnakes(){
		for(int i = clients.size()-1; i >= 0 ; i --){
			ClientBridge b = clients.get(i);
			if(b.getSnake().isLive()){
				b.updateSnake();
			}
			else{
				b.closeConnection();
				clients.remove(i);
				System.out.println("Snake at index " + i + " died.");
				System.out.println(clients.size() + " snakes remaining");
			}
		}
		//Send a copy of the arena to all of the clients
		for(int i = clients.size()-1; i >= 0; i --){
			clients.get(i).sendArena();
		}
	}

	/**
	 * @return all of the snake instances contained
	 * in the ClientBridges
	 */
	public synchronized Snake[] getSnakes(){
		Snake[] snakes = new Snake[clients.size()];
		for (int i = 0; i < clients.size(); i++) {
			snakes[i] = clients.get(i).getSnake();
		}
		return snakes;
	}
	
	/**
	 * Finds the lowest snake ID not currently occupied by a snake
	 * @return the lowest snake ID not currently occupied by a snake
	 */
	public synchronized byte getUniqueSnakeID(){
		byte id = -1;
		boolean hasMatch = true;
		while(hasMatch){
			id ++;
			hasMatch = false;
			for(ClientBridge c:clients){
				if(c.getSnake().getId()==id){hasMatch = true;break;}
			}
		}
		return id;
	}
}
