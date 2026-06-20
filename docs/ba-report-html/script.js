document.addEventListener('DOMContentLoaded', () => {
  // Mobile Sidebar Toggle
  const sidebar = document.getElementById('sidebar');
  const overlay = document.querySelector('.sidebar-overlay');
  const menuBtn = document.querySelector('.menu-btn');

  function toggleSidebar() {
    sidebar.classList.toggle('open');
    overlay.classList.toggle('open');
  }

  if (menuBtn) menuBtn.addEventListener('click', toggleSidebar);
  if (overlay) overlay.addEventListener('click', toggleSidebar);

  // Close sidebar on link click (mobile)
  const navLinks = document.querySelectorAll('nav ul li a');
  navLinks.forEach(link => {
    link.addEventListener('click', () => {
      if (window.innerWidth <= 900) {
        sidebar.classList.remove('open');
        overlay.classList.remove('open');
      }
    });
  });

  // Dark Mode Toggle
  const themeBtn = document.getElementById('theme-toggle');
  const themeIcon = themeBtn ? themeBtn.querySelector('.icon') : null;
  const currentTheme = localStorage.getItem('theme') || 'dark';

  document.documentElement.setAttribute('data-theme', currentTheme);
  updateThemeIcon(currentTheme);

  if (themeBtn) {
    themeBtn.addEventListener('click', () => {
      let theme = document.documentElement.getAttribute('data-theme');
      let newTheme = theme === 'dark' ? 'light' : 'dark';
      document.documentElement.setAttribute('data-theme', newTheme);
      localStorage.setItem('theme', newTheme);
      updateThemeIcon(newTheme);
    });
  }

  function updateThemeIcon(theme) {
    if (!themeIcon) return;
    if (theme === 'dark') {
      themeIcon.textContent = '☀️';
      themeBtn.title = 'Chuyển sang Giao diện Sáng';
    } else {
      themeIcon.textContent = '🌙';
      themeBtn.title = 'Chuyển sang Giao diện Tối';
    }
  }

  // ScrollSpy - Active Nav Highlighting
  const sections = document.querySelectorAll('.section');
  window.addEventListener('scroll', () => {
    let current = '';
    const scrollPos = window.scrollY + 100; // Offset for header

    sections.forEach(section => {
      const sectionTop = section.offsetTop;
      if (scrollPos >= sectionTop) {
        current = section.getAttribute('id');
      }
    });

    navLinks.forEach(link => {
      link.classList.remove('active');
      if (link.getAttribute('href') === `#${current}`) {
        link.classList.add('active');
        // Update breadcrumb
        const sectionTitleText = section.querySelector('.section-title')?.textContent || '';
        const breadcrumbSpan = document.querySelector('.breadcrumb span');
        if (breadcrumbSpan) {
          breadcrumbSpan.textContent = sectionTitleText.replace(/^[^\w\s]*\s*/, ''); // strip icons
        }
      }
    });

    // Progress Bar
    const winScroll = document.body.scrollTop || document.documentElement.scrollTop;
    const height = document.documentElement.scrollHeight - document.documentElement.clientHeight;
    const scrolled = (winScroll / height) * 100;
    const progress = document.getElementById('progress');
    if (progress) progress.style.width = scrolled + '%';

    // Back to top visibility
    const backTop = document.getElementById('back-top');
    if (backTop) {
      if (window.scrollY > 300) {
        backTop.classList.add('visible');
      } else {
        backTop.classList.remove('visible');
      }
    }
  });

  // Back to Top Click
  const backTop = document.getElementById('back-top');
  if (backTop) {
    backTop.addEventListener('click', () => {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    });
  }

  // Print Page
  const printBtn = document.getElementById('print-report');
  if (printBtn) {
    printBtn.addEventListener('click', () => {
      window.print();
    });
  }

  // Live Search
  const searchInput = document.getElementById('search-input');
  const searchResults = document.createElement('div');
  searchResults.id = 'search-results';
  searchResults.style.display = 'none';
  
  if (searchInput) {
    const parent = searchInput.parentNode;
    parent.appendChild(searchResults);

    searchInput.addEventListener('input', (e) => {
      const query = e.target.value.toLowerCase().trim();
      searchResults.innerHTML = '';

      if (!query) {
        searchResults.style.display = 'none';
        return;
      }

      const matches = [];
      
      // Search headers and text sections
      const headings = document.querySelectorAll('.section h2, .section h3, .section h4, .fr-item, td');
      headings.forEach((element) => {
        let text = element.textContent || '';
        let section = element.closest('.section');
        if (!section) return;
        
        let sectionId = section.getAttribute('id');
        let sectionTitle = section.querySelector('.section-title')?.textContent.trim() || 'Tài liệu';

        if (text.toLowerCase().includes(query)) {
          // Extract snippet
          let idx = text.toLowerCase().indexOf(query);
          let start = Math.max(0, idx - 30);
          let end = Math.min(text.length, idx + query.length + 30);
          let snippet = text.substring(start, end).replace(/\n/g, ' ');
          if (start > 0) snippet = '...' + snippet;
          if (end < text.length) snippet = snippet + '...';

          // Highlight matching term
          const regex = new RegExp(`(${query})`, 'gi');
          snippet = snippet.replace(regex, '<mark>$1</mark>');

          matches.push({
            id: sectionId,
            sectionName: sectionTitle,
            snippet: snippet,
            element: element
          });
        }
      });

      if (matches.length > 0) {
        searchResults.style.display = 'block';
        // Limit to 8 results
        matches.slice(0, 8).forEach(match => {
          const item = document.createElement('a');
          item.href = `#${match.id}`;
          item.innerHTML = `
            <div style="font-weight:700; font-size:12px; color:var(--accent);">${match.sectionName}</div>
            <div style="font-size:11px; margin-top:2px; color:var(--muted);">${match.snippet}</div>
          `;
          item.addEventListener('click', () => {
            searchResults.style.display = 'none';
            searchInput.value = '';
            // Flash matching element
            setTimeout(() => {
              match.element.scrollIntoView({ behavior: 'smooth', block: 'center' });
              match.element.style.transition = 'background-color 0.5s ease';
              match.element.style.backgroundColor = 'rgba(200,16,46,0.2)';
              setTimeout(() => {
                match.element.style.backgroundColor = '';
              }, 2000);
            }, 300);
          });
          searchResults.appendChild(item);
        });
      } else {
        searchResults.style.display = 'block';
        searchResults.innerHTML = '<div style="padding:10px 14px; font-size:12px; color:var(--muted);">Không tìm thấy kết quả</div>';
      }
    });

    // Close search results when clicking outside
    document.addEventListener('click', (e) => {
      if (!searchInput.contains(e.target) && !searchResults.contains(e.target)) {
        searchResults.style.display = 'none';
      }
    });
  }
});
