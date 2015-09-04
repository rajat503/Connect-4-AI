import java.io.*;
import java.util.Scanner;
import java.util.Arrays;
import java.util.ArrayList;
import java.awt.Point;

public class State implements Cloneable
{

	int rows, cols;
	char[][] board;

	/* basic methods for constructing and proper hashing of State objects */
	public State(int n_rows, int n_cols){
		this.rows=n_rows;
		this.cols=n_cols;
		this.board=new char[n_rows][n_cols];

		//fill the board up with blanks
		for(int i=0; i<n_rows; i++)
			for(int j=0; j<n_cols; j++)
				this.board[i][j]='.';
	}

	public boolean equals(Object obj){
		//have faith and cast
		State other=(State)obj;
		return Arrays.deepEquals(this.board, other.board);
	}

	public int hashCode(){
		String b="";
		for(int i=0; i<board.length; i++)
			b+=String.valueOf(board[0]);
		return b.hashCode();
	}

	public Object clone() throws CloneNotSupportedException {
        State new_state=new State(this.rows, this.cols);
		for (int i=0; i<this.rows; i++)
			new_state.board[i] = (char[]) this.board[i].clone();
		return new_state;
	}



	/* returns a list of actions that can be taken from the current state
	actions are integers representing the column where a coin can be dropped */
	public ArrayList<Integer> getLegalActions(){
		ArrayList<Integer> actions=new ArrayList<Integer>();
		for(int j=0; j<this.cols; j++)
			if(this.board[0][j]=='.')
				actions.add(j);
		return actions;
	}

	/* returns a State object that is obtained by the agent (parameter)
	performing an action (parameter) on the current state */
	public State generateSuccessor(char agent, int action) throws CloneNotSupportedException{

		int row;
		for(row=0; row<this.rows && this.board[row][action]!='X' && this.board[row][action]!='O'; row++);
		State new_state=(State)this.clone();
		new_state.board[row-1][action]=agent;

		return new_state;
	}

	/* Print's the current state's board in a nice pretty way */
	public void printBoard(){
		System.out.println(new String(new char[this.cols*2]).replace('\0', '-'));
		for(int i=0; i<this.rows; i++){
			for(int j=0; j<this.cols; j++){
				System.out.print(this.board[i][j]+" ");
			}
			System.out.println();
		}
		System.out.println(new String(new char[this.cols*2]).replace('\0', '-'));
	}

	/* returns True/False if the agent(parameter) has won the game
	by checking all rows/columns/diagonals for a sequence of >=4 */
	public boolean isGoal(char agent){

		String find=""+agent+""+agent+""+agent+""+agent;

		//check rows
		for(int i=0; i<this.rows; i++)
			if(String.valueOf(this.board[i]).contains(find))
				return true;

		//check cols
		for(int j=0; j<this.cols; j++){
			String col="";
			for(int i=0; i<this.rows; i++)
				col+=this.board[i][j];

			if(col.contains(find))
				return true;
		}

		//check diags
		ArrayList<Point> pos_right=new ArrayList<Point>();
		ArrayList<Point> pos_left=new ArrayList<Point>();

		for(int j=0; j<this.cols-4+1; j++)
			pos_right.add(new Point(0,j));
		for(int j=4-1; j<this.cols; j++)
			pos_left.add(new Point(0,j));
		for(int i=1; i<this.rows-4+1; i++){
			pos_right.add(new Point(i,0));
			pos_left.add(new Point(i,this.cols-1));
		}

		//check right diags
		for (Point p : pos_right) {
			String d="";
			int x=p.x, y=p.y;
			while(true){
				if (x>=this.rows||y>=this.cols)
					break;
				d+=this.board[x][y];
				x+=1; y+=1;
			}
			if(d.contains(find))
				return true;
		}

		//check left diags
		for (Point p : pos_left) {
			String d="";
			int x=p.x, y=p.y;
			while(true){
				if(y<0||x>=this.rows||y>=this.cols)
					break;
				d+=this.board[x][y];
				x+=1; y-=1;
			}
			if(d.contains(find))
				return true;
		}

		return false;

	}


	/* returns the value of each state for minimax to min/max over at
	zero depth. Right now it's pretty trivial, looking for only goal states.
	(This would be perfect for infinite depth minimax. Not so great for d=2) */
	public double evaluationFunction(){

		if (this.isGoal('O'))
			return 1000.0;
		if (this.isGoal('X'))
			return -1000.0;
		return 0.0;
	}




	public static void main(String[] args) throws CloneNotSupportedException{
		int depth=1;
		MinimaxAgent mma = new MinimaxAgent(depth);
		State s=new State(7,7);
		while(true){
			int action = mma.getAction(s);
			s = s.generateSuccessor('O', action);
			s.printBoard();
			//check if O won? break
			if(s.isGoal('O'))
			{
				System.out.println("O won");
				break;
			}
			Scanner st=new Scanner(System.in);
			int enemy_move = st.nextInt();
			s = s.generateSuccessor('X', enemy_move);
			if(s.isGoal('X'))
			{
				System.out.println("X won");
				break;
			}
			//check if X won? break
			//pause
		}
	}
}
class MinimaxAgent
{
	private int depth;
	public MinimaxAgent(int depth)
	{
		this.depth=depth*2;
	}
	public int getAction(State s) throws CloneNotSupportedException
	{
		ArrayList<Double> actionsval=new ArrayList<Double>();
		ArrayList<Integer> list=s.getLegalActions();
		double v=-10000.0;
		int action=0;
		for (int i=0;i<list.size() ;i++ )
		{
			actionsval.add(value(s.generateSuccessor('O',list.get(i)),1));
		}
		for (int i=0;i<list.size() ;i++ )
		{
				if(actionsval.get(i)>v)
				{
					v=actionsval.get(i);
					action=list.get(i);
				}
		}
		return action;
	}
	public double value(State s, int d) throws CloneNotSupportedException
	{
		if(d!=depth)
		{
			if(d%2==0)
			{
				return maxv(s,d);
			}
			else
			{
				return minv(s,d);
			}
		}
		else
		{
			return s.evaluationFunction();
		}

	}
	public double maxv(State s, int d) throws CloneNotSupportedException
	{
		double v=-10000.0;
		ArrayList<Integer> list=s.getLegalActions();
		for (int i=0;i<list.size() ;i++ )
		{
			v=Math.max(v,value(s.generateSuccessor('O',list.get(i)),d+1));
		}
		return v;
	}
	public double minv(State s, int d) throws CloneNotSupportedException
	{
		double v=10000.0;
		ArrayList<Integer> list=s.getLegalActions();
		for (int i=0;i<list.size() ;i++ )
		{
			v=Math.min(v,value(s.generateSuccessor('X',list.get(i)),d+1));
		}
		return v;
	}
}
