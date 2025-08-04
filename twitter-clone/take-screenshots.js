const puppeteer = require('puppeteer');
const fs = require('fs');
const path = require('path');

async function takeScreenshots() {
    console.log('📸 Taking Screenshots of RSS Feed Hub Application');
    console.log('==================================================');

    // Create screenshots directory
    const screenshotsDir = path.join(__dirname, '..', 'screenshots');
    if (!fs.existsSync(screenshotsDir)) {
        fs.mkdirSync(screenshotsDir, { recursive: true });
    }

    let browser;
    try {
        // Launch browser
        console.log('🚀 Launching browser...');
        browser = await puppeteer.launch({
            headless: true,
            args: [
                '--no-sandbox',
                '--disable-setuid-sandbox',
                '--disable-dev-shm-usage',
                '--disable-gpu',
                '--window-size=1920,1080'
            ]
        });

        const page = await browser.newPage();
        await page.setViewport({ width: 1920, height: 1080 });

        // Function to take screenshot with error handling
        async function takeScreenshot(url, filename, description, waitTime = 3000) {
            console.log(`\n📷 Taking screenshot: ${description}`);
            console.log(`   URL: ${url}`);
            console.log(`   File: screenshots/${filename}`);

            try {
                await page.goto(url, { waitUntil: 'networkidle0', timeout: 30000 });
                await new Promise(resolve => setTimeout(resolve, waitTime));

                const screenshotPath = path.join(screenshotsDir, filename);
                await page.screenshot({ 
                    path: screenshotPath, 
                    fullPage: true,
                    type: 'png'
                });

                if (fs.existsSync(screenshotPath)) {
                    const stats = fs.statSync(screenshotPath);
                    const fileSizeInBytes = stats.size;
                    const fileSizeInKB = (fileSizeInBytes / 1024).toFixed(2);
                    console.log(`   ✅ Screenshot saved: screenshots/${filename}`);
                    console.log(`   📊 File size: ${fileSizeInKB} KB`);
                } else {
                    console.log(`   ❌ Failed to save screenshot: ${filename}`);
                }
            } catch (error) {
                console.log(`   ❌ Error taking screenshot: ${error.message}`);
            }
        }

        // Check if the application is running
        console.log('\n⏳ Checking if applications are ready...');
        
        try {
            await page.goto('http://localhost:3000', { timeout: 10000 });
            console.log('✅ Frontend is responding on port 3000');
        } catch (error) {
            console.log('❌ Frontend is not responding on port 3000');
            console.log('   Make sure the React app is running with: npm start');
            return;
        }

        console.log('\n📸 Starting screenshot capture...');

        // Take screenshots of the main application
        await takeScreenshot('http://localhost:3000', '01_homepage_initial.png', 'Homepage - Initial Load', 2000);
        
        // Wait a bit more for dynamic content to load
        await takeScreenshot('http://localhost:3000', '02_homepage_loaded.png', 'Homepage - After Content Load', 5000);

        // Take screenshot of the feed view (default view)
        await takeScreenshot('http://localhost:3000', '03_feed_view.png', 'Main Feed View', 3000);

        // Try to interact with the sidebar to show different views
        try {
            console.log('\n🖱️  Attempting to navigate to different sections...');
            
            // Click on RSS Manager if available
            await page.goto('http://localhost:3000');
            await new Promise(resolve => setTimeout(resolve, 3000));
            
            // Look for sidebar navigation elements
            const sidebarButtons = await page.$$('button, .nav-item, .sidebar-item');
            console.log(`   Found ${sidebarButtons.length} potential navigation elements`);
            
            if (sidebarButtons.length > 1) {
                // Try clicking the second button (might be RSS Manager)
                await sidebarButtons[1].click();
                await new Promise(resolve => setTimeout(resolve, 2000));
                await takeScreenshot('http://localhost:3000', '04_rss_manager.png', 'RSS Manager View', 3000);
            }

            if (sidebarButtons.length > 2) {
                // Try clicking the third button (might be Leaderboard)
                await sidebarButtons[2].click();
                await new Promise(resolve => setTimeout(resolve, 2000));
                await takeScreenshot('http://localhost:3000', '05_leaderboard.png', 'Leaderboard View', 3000);
            }

            // Go back to home view
            if (sidebarButtons.length > 0) {
                await sidebarButtons[0].click();
                await new Promise(resolve => setTimeout(resolve, 2000));
                await takeScreenshot('http://localhost:3000', '06_back_to_home.png', 'Back to Home View', 3000);
            }

        } catch (error) {
            console.log(`   ⚠️  Navigation interaction failed: ${error.message}`);
        }

        // Take a final comprehensive screenshot
        await page.goto('http://localhost:3000');
        await new Promise(resolve => setTimeout(resolve, 5000));
        await takeScreenshot('http://localhost:3000', '07_final_view.png', 'Final Application View', 5000);

        console.log('\n🎉 Screenshot capture completed!');

    } catch (error) {
        console.error('❌ Error during screenshot process:', error);
    } finally {
        if (browser) {
            await browser.close();
        }
    }

    // Show summary
    console.log('\n📁 Screenshots saved in the screenshots/ directory:');
    try {
        const files = fs.readdirSync(screenshotsDir).filter(file => 
            file.endsWith('.png') || file.endsWith('.jpg') || file.endsWith('.jpeg')
        );
        
        if (files.length > 0) {
            files.forEach(file => {
                const filePath = path.join(screenshotsDir, file);
                const stats = fs.statSync(filePath);
                const fileSizeInKB = (stats.size / 1024).toFixed(2);
                console.log(`   📄 ${file} (${fileSizeInKB} KB)`);
            });
            
            console.log(`\n📊 Screenshot Summary:`);
            console.log(`   Total screenshots: ${files.length}`);
            
            const totalSize = files.reduce((sum, file) => {
                const filePath = path.join(screenshotsDir, file);
                return sum + fs.statSync(filePath).size;
            }, 0);
            console.log(`   Total size: ${(totalSize / 1024).toFixed(2)} KB`);
        } else {
            console.log('   No image files found');
        }
    } catch (error) {
        console.log('   Error reading screenshots directory');
    }
}

// Run the screenshot function
takeScreenshots().catch(console.error);