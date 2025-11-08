from flask import Flask, request, jsonify
import random

app = Flask(__name__)

@app.route('/move', methods=['POST'])
def get_move():
    # Get the board from the request body
    data = request.get_json()
    if not data or 'board' not in data:
        return jsonify({'error': 'Invalid request format'}), 400

    board = data['board']

    # Find all empty cells
    empty_cells = []
    for r_idx, row in enumerate(board):
        for c_idx, cell in enumerate(row):
            if not cell:  # Check for empty string or null
                empty_cells.append((r_idx, c_idx))

    # Choose a random empty cell for the move
    if not empty_cells:
        # No available moves, though the game should end before this
        return jsonify({'error': 'No available moves'}), 400

    # Select a random move from the available options
    chosen_move = random.choice(empty_cells)

    # Return the move in the expected format
    # e.g., {"move": [row, col]}
    response_data = {'move': [chosen_move[0], chosen_move[1]]}

    print(f"Received board, sending move: {response_data}")

    return jsonify(response_data)

if __name__ == '__main__':
    # Run the server on 0.0.0.0 to make it accessible
    # from the Android emulator, using the correct port.
    app.run(host='0.0.0.0', port=8080, debug=True)

