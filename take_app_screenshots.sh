#!/bin/bash

echo "📸 Taking Screenshots of RSS Feed Hub Application"
echo "=================================================="

# Create screenshots directory if it doesn't exist
mkdir -p screenshots

# Start Xvfb for headless display
export DISPLAY=:99
Xvfb :99 -screen 0 1920x1080x24 &
XVFB_PID=$!

# Wait for Xvfb to start
sleep 3

# Function to take screenshot with better error handling
take_screenshot() {
    local url=$1
    local filename=$2
    local description=$3
    local wait_time=${4:-3}
    
    echo "📷 Taking screenshot: $description"
    echo "   URL: $url"
    echo "   File: screenshots/$filename"
    
    # Use chromium with more options for better rendering
    timeout 30 chromium-browser \
        --headless \
        --disable-gpu \
        --disable-software-rasterizer \
        --disable-background-timer-throttling \
        --disable-backgrounding-occluded-windows \
        --disable-renderer-backgrounding \
        --disable-features=TranslateUI \
        --disable-extensions \
        --disable-plugins \
        --disable-default-apps \
        --no-sandbox \
        --window-size=1920,1080 \
        --screenshot=screenshots/$filename \
        --virtual-time-budget=5000 \
        $url
    
    # Wait a bit for the file to be written
    sleep 1
    
    if [ -f "screenshots/$filename" ]; then
        echo "   ✅ Screenshot saved: screenshots/$filename"
        # Get file size
        size=$(du -h screenshots/$filename | cut -f1)
        echo "   📊 File size: $size"
    else
        echo "   ❌ Failed to take screenshot: $filename"
    fi
    echo ""
}

# Wait for applications to be ready
echo "⏳ Waiting for applications to be ready..."
sleep 10

# Check if frontend is responding
if curl -s http://localhost:3000 > /dev/null; then
    echo "✅ Frontend is responding on port 3000"
else
    echo "❌ Frontend is not responding on port 3000"
    exit 1
fi

# Check if backend is responding
if curl -s http://localhost:8080 > /dev/null; then
    echo "✅ Backend is responding on port 8080"
else
    echo "⚠️  Backend may not be fully ready on port 8080"
fi

echo ""
echo "📸 Starting screenshot capture..."
echo ""

# Take screenshots of different views
take_screenshot "http://localhost:3000" "01_homepage_feed.png" "Homepage - Main Feed View" 5

# Try to navigate to different sections by using URL fragments or query parameters
# Since this is a SPA, we'll take screenshots after some delay to let content load
take_screenshot "http://localhost:3000" "02_homepage_loaded.png" "Homepage - After Content Load" 8

# Take additional screenshots with different wait times to capture dynamic content
take_screenshot "http://localhost:3000" "03_homepage_full.png" "Homepage - Full Interface" 10

# If the app has routing or different views, we can try accessing them directly
# Based on the React code, it seems to use state-based navigation, so we'll capture what we can
take_screenshot "http://localhost:3000" "04_app_interface.png" "Application Interface" 12

# Take a final screenshot after everything has loaded
sleep 5
take_screenshot "http://localhost:3000" "05_final_view.png" "Final Application View" 15

# Clean up
kill $XVFB_PID 2>/dev/null

echo "🎉 Screenshot capture completed!"
echo ""
echo "📁 Screenshots saved in the screenshots/ directory:"
ls -la screenshots/ | grep -E '\.(png|jpg|jpeg)$' || echo "No image files found"

echo ""
echo "📊 Screenshot Summary:"
if [ -d "screenshots" ]; then
    count=$(ls screenshots/*.png 2>/dev/null | wc -l)
    echo "   Total screenshots: $count"
    if [ $count -gt 0 ]; then
        echo "   Total size: $(du -sh screenshots/ | cut -f1)"
    fi
fi