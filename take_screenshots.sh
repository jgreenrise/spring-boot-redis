#!/bin/bash

# Create screenshots directory if it doesn't exist
mkdir -p screenshots

# Start Xvfb for headless display
export DISPLAY=:99
Xvfb :99 -screen 0 1920x1080x24 &
XVFB_PID=$!

# Wait for Xvfb to start
sleep 2

# Function to take screenshot
take_screenshot() {
    local url=$1
    local filename=$2
    local description=$3
    
    echo "Taking screenshot: $description"
    chromium-browser --headless --disable-gpu --window-size=1920,1080 --screenshot=screenshots/$filename $url
    
    if [ -f "screenshots/$filename" ]; then
        echo "✓ Screenshot saved: screenshots/$filename"
    else
        echo "✗ Failed to take screenshot: $filename"
    fi
}

# Wait for applications to be ready
echo "Waiting for applications to be ready..."
sleep 5

# Take screenshots of different parts of the application
take_screenshot "http://localhost:3000" "01_homepage.png" "Homepage with flashcards"
take_screenshot "http://localhost:3000" "02_flashcard_interface.png" "Flashcard interface"

# Take a screenshot after waiting a bit more for content to load
sleep 3
take_screenshot "http://localhost:3000" "03_loaded_flashcards.png" "Loaded flashcards view"

# Clean up
kill $XVFB_PID

echo "Screenshots completed!"
ls -la screenshots/