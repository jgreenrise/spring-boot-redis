const puppeteer = require('puppeteer');
const fs = require('fs');
const path = require('path');

async function takeCompleteScreenshots() {
    console.log('📸 Taking Complete Screenshots of RSS Feed Hub Application');
    console.log('=========================================================');

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

        // Enable console logging from the page
        page.on('console', msg => {
            if (msg.type() === 'error') {
                console.log('   🔍 Page Error:', msg.text());
            }
        });

        // Function to take screenshot with error handling
        async function takeScreenshot(filename, description, waitTime = 3000) {
            console.log(`\n📷 Taking screenshot: ${description}`);
            console.log(`   File: screenshots/${filename}`);

            try {
                await new Promise(resolve => setTimeout(resolve, waitTime));

                const screenshotPath = path.join(screenshotsDir, filename);
                await page.screenshot({ 
                    path: screenshotPath, 
                    fullPage: true,
                    type: 'png'
                });

                if (fs.existsSync(screenshotPath)) {
                    const stats = fs.statSync(screenshotPath);
                    const fileSizeInKB = (stats.size / 1024).toFixed(2);
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
            await page.goto('http://localhost:3000', { waitUntil: 'networkidle0', timeout: 15000 });
            console.log('✅ Frontend is responding on port 3000');
        } catch (error) {
            console.log('❌ Frontend is not responding on port 3000');
            console.log('   Make sure the React app is running with: npm start');
            return;
        }

        console.log('\n📸 Starting comprehensive screenshot capture...');

        // Take initial screenshot
        await takeScreenshot('app_01_initial_load.png', 'Initial Application Load', 2000);

        // Navigate through all views systematically
        const views = [
            { name: 'Home', selector: 'button[class*="nav-item"]:first-child', filename: 'app_02_home_view.png' },
            { name: 'RSS Feeds', selector: 'button[class*="nav-item"]:nth-child(2)', filename: 'app_03_rss_feeds_view.png' },
            { name: 'Leaderboard', selector: 'button[class*="nav-item"]:nth-child(3)', filename: 'app_04_leaderboard_view.png' }
        ];

        for (const view of views) {
            try {
                console.log(`\n🖱️  Navigating to ${view.name} view...`);
                
                // Try different selector strategies
                let clicked = false;
                
                // Strategy 1: Try the specific selector
                try {
                    await page.click(view.selector);
                    clicked = true;
                    console.log(`   ✅ Clicked ${view.name} button using selector: ${view.selector}`);
                } catch (error) {
                    console.log(`   ⚠️  Selector ${view.selector} failed: ${error.message}`);
                }

                // Strategy 2: Try clicking by text content
                if (!clicked) {
                    try {
                        await page.evaluate((viewName) => {
                            const buttons = Array.from(document.querySelectorAll('button'));
                            const targetButton = buttons.find(btn => 
                                btn.textContent && btn.textContent.includes(viewName)
                            );
                            if (targetButton) {
                                targetButton.click();
                                return true;
                            }
                            return false;
                        }, view.name);
                        clicked = true;
                        console.log(`   ✅ Clicked ${view.name} button by text content`);
                    } catch (error) {
                        console.log(`   ⚠️  Text-based click failed: ${error.message}`);
                    }
                }

                // Strategy 3: Try finding nav items by class
                if (!clicked) {
                    try {
                        const navItems = await page.$$('.nav-item');
                        if (navItems.length > 0) {
                            const index = views.indexOf(view);
                            if (index < navItems.length) {
                                await navItems[index].click();
                                clicked = true;
                                console.log(`   ✅ Clicked ${view.name} button by index: ${index}`);
                            }
                        }
                    } catch (error) {
                        console.log(`   ⚠️  Index-based click failed: ${error.message}`);
                    }
                }

                if (clicked) {
                    await takeScreenshot(view.filename, `${view.name} View`, 3000);
                } else {
                    console.log(`   ❌ Could not navigate to ${view.name} view`);
                    await takeScreenshot(`failed_${view.filename}`, `Failed ${view.name} View`, 1000);
                }

            } catch (error) {
                console.log(`   ❌ Error navigating to ${view.name}: ${error.message}`);
            }
        }

        // Take some additional screenshots with different interactions
        console.log('\n📸 Taking additional screenshots...');

        // Go back to home and take a final screenshot
        try {
            await page.evaluate(() => {
                const buttons = Array.from(document.querySelectorAll('button'));
                const homeButton = buttons.find(btn => 
                    btn.textContent && btn.textContent.includes('Home')
                );
                if (homeButton) {
                    homeButton.click();
                }
            });
            await takeScreenshot('app_05_final_home_view.png', 'Final Home View', 3000);
        } catch (error) {
            console.log('   ⚠️  Could not return to home view');
        }

        // Take a screenshot of the page source for debugging
        try {
            const content = await page.content();
            const debugPath = path.join(screenshotsDir, 'debug_page_source.html');
            fs.writeFileSync(debugPath, content);
            console.log('   📄 Page source saved for debugging: screenshots/debug_page_source.html');
        } catch (error) {
            console.log('   ⚠️  Could not save page source');
        }

        console.log('\n🎉 Complete screenshot capture finished!');

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
            console.log('\n📋 Screenshot Files:');
            files.sort().forEach(file => {
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

    console.log('\n🎯 Application Overview:');
    console.log('   This is an RSS Feed Hub application with a Twitter-like interface');
    console.log('   📱 Main Views:');
    console.log('      • Home/Feed - Main RSS feed display');
    console.log('      • RSS Feeds - RSS feed management interface');
    console.log('      • Leaderboard - User rankings and statistics');
    console.log('   🏗️  Architecture: React frontend + Spring Boot backend + Redis');
    console.log('   🌐 URLs: Frontend (http://localhost:3000), Backend (http://localhost:8080)');
}

// Run the screenshot function
takeCompleteScreenshots().catch(console.error);