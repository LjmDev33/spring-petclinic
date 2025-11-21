/**
 * Uppy Browser Bundle Wrapper
 * 브라우저에서 사용 가능한 Uppy 전역 객체 생성
 * 
 * @author Jeongmin Lee
 * @description 로컬 내장 Uppy 라이브러리를 브라우저 전역 객체로 노출
 * 
 * 사용법:
 * const uppy = Uppy.Core({...});
 * uppy.use(Uppy.Dashboard, {...});
 * uppy.use(Uppy.XHRUpload, {...});
 */

(function(window, document) {
  'use strict';

  // Uppy 전역 네임스페이스 생성
  window.Uppy = window.Uppy || {};

  /**
   * Uppy Core 생성자
   * @param {Object} opts - Uppy 옵션
   */
  window.Uppy.Core = function(opts) {
    this.opts = Object.assign({
      id: 'uppy',
      autoProceed: false,
      allowMultipleUploads: true,
      debug: false,
      restrictions: {
        maxFileSize: null,
        maxNumberOfFiles: null,
        minNumberOfFiles: null,
        allowedFileTypes: null
      },
      meta: {},
      onBeforeFileAdded: function(currentFile) { return currentFile; },
      onBeforeUpload: function(files) { return files; },
      locale: {},
      store: null
    }, opts || {});

    this.plugins = [];
    this.files = {};
    this.state = {
      files: {},
      capabilities: {
        uploadProgress: true,
        individualCancellation: true,
        resumableUploads: false
      },
      totalProgress: 0
    };

    this.events = {};
  };

  /**
   * 플러그인 사용
   */
  window.Uppy.Core.prototype.use = function(Plugin, opts) {
    if (typeof Plugin === 'function') {
      var plugin = new Plugin(this, opts);
      this.plugins.push(plugin);
      if (plugin.install) {
        plugin.install();
      }
    }
    return this;
  };

  /**
   * 파일 추가
   */
  window.Uppy.Core.prototype.addFile = function(fileData) {
    var fileId = this.generateFileId(fileData);
    var file = {
      id: fileId,
      name: fileData.name || 'noname',
      type: fileData.type || 'application/octet-stream',
      data: fileData.data,
      size: fileData.size || fileData.data.size,
      meta: Object.assign({}, this.opts.meta, fileData.meta || {}),
      progress: {
        uploadStarted: null,
        uploadComplete: false,
        percentage: 0,
        bytesUploaded: 0,
        bytesTotal: fileData.size || fileData.data.size
      }
    };
    this.files[fileId] = file;
    this.state.files[fileId] = file;
    this.emit('file-added', file);
    return fileId;
  };

  /**
   * 파일 ID 생성
   */
  window.Uppy.Core.prototype.generateFileId = function(file) {
    return 'uppy-' + file.name.toLowerCase().replace(/[^a-z0-9]/g, '-') + '-' + Date.now();
  };

  /**
   * 파일 가져오기
   */
  window.Uppy.Core.prototype.getFile = function(fileId) {
    return this.files[fileId];
  };

  /**
   * 모든 파일 가져오기
   */
  window.Uppy.Core.prototype.getFiles = function() {
    return Object.values(this.files);
  };

  /**
   * 파일 제거
   */
  window.Uppy.Core.prototype.removeFile = function(fileId) {
    var file = this.files[fileId];
    if (file) {
      delete this.files[fileId];
      delete this.state.files[fileId];
      this.emit('file-removed', file);
    }
  };

  /**
   * 업로드 시작
   */
  window.Uppy.Core.prototype.upload = function() {
    var self = this;
    var files = this.getFiles();
    
    if (files.length === 0) {
      return Promise.resolve({ successful: [], failed: [] });
    }

    this.emit('upload-start', files);

    // 각 업로더 플러그인에게 업로드 요청
    var uploaderPlugins = this.plugins.filter(function(p) {
      return p.type === 'uploader';
    });

    if (uploaderPlugins.length === 0) {
      console.warn('No uploader plugins found');
      return Promise.resolve({ successful: [], failed: [] });
    }

    // 첫 번째 업로더 사용
    var uploader = uploaderPlugins[0];
    return uploader.uploadFiles(files);
  };

  /**
   * 이벤트 등록
   */
  window.Uppy.Core.prototype.on = function(event, callback) {
    if (!this.events[event]) {
      this.events[event] = [];
    }
    this.events[event].push(callback);
    return this;
  };

  /**
   * 이벤트 발생
   */
  window.Uppy.Core.prototype.emit = function(event) {
    var args = Array.prototype.slice.call(arguments, 1);
    if (this.events[event]) {
      this.events[event].forEach(function(callback) {
        callback.apply(null, args);
      });
    }
  };

  /**
   * 상태 업데이트
   */
  window.Uppy.Core.prototype.setState = function(patch) {
    this.state = Object.assign({}, this.state, patch);
  };

  /**
   * 상태 가져오기
   */
  window.Uppy.Core.prototype.getState = function() {
    return this.state;
  };

  /**
   * Dashboard 플러그인
   */
  window.Uppy.Dashboard = function(uppy, opts) {
    this.uppy = uppy;
    this.type = 'acquirer';
    this.id = 'Dashboard';
    this.opts = Object.assign({
      target: null,
      inline: false,
      width: 750,
      height: 200,
      thumbnailWidth: 280,
      showLinkToFileUploadResult: false,
      showProgressDetails: false,
      hideUploadButton: false,
      hideRetryButton: false,
      hidePauseResumeButton: false,
      hideCancelButton: false,
      hideProgressAfterFinish: false,
      note: null,
      closeModalOnClickOutside: false,
      closeAfterFinish: false,
      disableStatusBar: false,
      disableInformer: false,
      disableThumbnailGenerator: false,
      disablePageScrollWhenModalOpen: true,
      proudlyDisplayPoweredByUppy: true,
      locale: {}
    }, opts || {});
  };

  /**
   * Dashboard 설치
   */
  window.Uppy.Dashboard.prototype.install = function() {
    var self = this;
    var target = typeof this.opts.target === 'string' 
      ? document.querySelector(this.opts.target)
      : this.opts.target;

    if (!target) {
      console.error('Dashboard target not found:', this.opts.target);
      return;
    }

    // Dashboard HTML 생성
    var html = '<div class="uppy-Dashboard" style="min-height: ' + this.opts.height + 'px;">';
    html += '  <div class="uppy-Dashboard-inner">';
    html += '    <div class="uppy-Dashboard-innerWrap">';
    html += '      <div class="uppy-Dashboard-dropFilesHereHint">';
    html += '        📁 파일을 여기에 드롭하세요';
    html += '      </div>';
    html += '      <div class="uppy-DashboardContent-bar">';
    html += '        <div class="uppy-DashboardContent-title">';
    html += '          <div style="margin-bottom: 0.5rem;">파일을 드래그하거나 아래 버튼을 클릭하세요</div>';
    html += '          <button type="button" class="uppy-Dashboard-browse">📎 파일 선택</button>';
    html += '        </div>';
    html += '      </div>';
    html += '      <div class="uppy-Dashboard-files" role="list"></div>';
    html += '      <input type="file" class="uppy-Dashboard-input" style="display: none;" multiple>';
    html += '    </div>';
    html += '  </div>';
    html += '</div>';

    target.innerHTML = html;

    console.log('Uppy Dashboard HTML inserted into:', this.opts.target);

    // 파일 선택 버튼
    var browseBtn = target.querySelector('.uppy-Dashboard-browse');
    var fileInput = target.querySelector('.uppy-Dashboard-input');
    
    if (browseBtn && fileInput) {
      browseBtn.addEventListener('click', function(e) {
        e.preventDefault();
        fileInput.click();
      });

      // 파일 타입 제한 적용
      if (self.uppy.opts.restrictions && self.uppy.opts.restrictions.allowedFileTypes) {
        fileInput.accept = self.uppy.opts.restrictions.allowedFileTypes.join(',');
      }

      // 파일 개수 제한 적용
      if (self.uppy.opts.restrictions && self.uppy.opts.restrictions.maxNumberOfFiles === 1) {
        fileInput.removeAttribute('multiple');
      }

      fileInput.addEventListener('change', function(e) {
        var files = Array.from(e.target.files);
        files.forEach(function(file) {
          try {
            self.uppy.addFile({
              name: file.name,
              type: file.type,
              data: file,
              size: file.size
            });
          } catch (err) {
            console.error('Error adding file:', err);
            alert('파일 추가 중 오류가 발생했습니다: ' + err.message);
          }
        });
        self.render();
      });
    }

    // Drag & Drop
    var dropArea = target.querySelector('.uppy-Dashboard-inner');
    var dashboardRoot = target.querySelector('.uppy-Dashboard');

    if (dropArea) {
      // dragenter 이벤트 처리 (드래그 시작 시)
      dropArea.addEventListener('dragenter', function(e) {
        e.preventDefault();
        e.stopPropagation();
        dropArea.classList.add('uppy-Dashboard-inner--isDraggingOver');
      });

      // dragover 이벤트 처리 (드래그 중)
      dropArea.addEventListener('dragover', function(e) {
        e.preventDefault();
        e.stopPropagation();
        dropArea.classList.add('uppy-Dashboard-inner--isDraggingOver');
      });

      // dragleave 이벤트 처리 (드래그 벗어남)
      dropArea.addEventListener('dragleave', function(e) {
        e.preventDefault();
        e.stopPropagation();
        // 자식 요소로 이동할 때는 클래스 제거하지 않음
        if (e.target === dropArea) {
          dropArea.classList.remove('uppy-Dashboard-inner--isDraggingOver');
        }
      });

      // drop 이벤트 처리 (파일 드롭)
      dropArea.addEventListener('drop', function(e) {
        e.preventDefault();
        e.stopPropagation();
        dropArea.classList.remove('uppy-Dashboard-inner--isDraggingOver');
        
        console.log('Files dropped:', e.dataTransfer.files.length);

        var files = Array.from(e.dataTransfer.files);
        files.forEach(function(file) {
          try {
            self.uppy.addFile({
              name: file.name,
              type: file.type,
              data: file,
              size: file.size
            });
          } catch (err) {
            console.error('Error adding file:', err);
            alert('파일 추가 중 오류가 발생했습니다: ' + err.message);
          }
        });
        self.render();
      });
    }

    // 전체 Dashboard에서도 기본 동작 방지
    if (dashboardRoot) {
      dashboardRoot.addEventListener('dragover', function(e) {
        e.preventDefault();
        e.stopPropagation();
      });

      dashboardRoot.addEventListener('drop', function(e) {
        e.preventDefault();
        e.stopPropagation();
      });
    }

    // 전체 페이지에서 드래그 앤 드롭 기본 동작 방지 (Dashboard 외부 드롭 방지)
    var preventDefaults = function(e) {
      e.preventDefault();
      e.stopPropagation();
    };

    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(function(eventName) {
      document.body.addEventListener(eventName, preventDefaults, false);
    });

    // 파일 추가 이벤트 리스너
    this.uppy.on('file-added', function(file) {
      self.render();
    });

    this.uppy.on('file-removed', function(file) {
      self.render();
    });

    this.uppy.on('upload-progress', function(file, progress) {
      self.updateFileProgress(file, progress);
    });

    this.filesContainer = target.querySelector('.uppy-Dashboard-files');
  };

  /**
   * 파일 목록 렌더링
   */
  window.Uppy.Dashboard.prototype.render = function() {
    if (!this.filesContainer) {
      console.warn('Files container not found');
      return;
    }

    var files = this.uppy.getFiles();
    var html = '';

    if (files.length === 0) {
      html = '<div class="uppy-Dashboard-empty">아직 선택된 파일이 없습니다.</div>';
    } else {
      files.forEach(function(file) {
        var sizeStr = (file.size / 1024 / 1024).toFixed(2) + ' MB';
        var fileIcon = '📄';

        // 파일 타입별 아이콘
        if (file.type && file.type.startsWith('image/')) {
          fileIcon = '🖼️';
        } else if (file.type === 'application/pdf') {
          fileIcon = '📑';
        } else if (file.type && (file.type.includes('word') || file.name.endsWith('.doc') || file.name.endsWith('.docx'))) {
          fileIcon = '📝';
        } else if (file.name.endsWith('.zip')) {
          fileIcon = '📦';
        }

        html += '<div class="uppy-Dashboard-Item" data-file-id="' + file.id + '">';
        html += '  <div class="uppy-Dashboard-Item-preview">';
        html += '    <div class="uppy-Dashboard-Item-previewIcon">' + fileIcon + '</div>';
        html += '  </div>';
        html += '  <div class="uppy-Dashboard-Item-info">';
        html += '    <h4 class="uppy-Dashboard-Item-name">' + file.name + '</h4>';
        html += '    <div class="uppy-Dashboard-Item-status">';
        html += '      <span class="uppy-Dashboard-Item-statusSize">' + sizeStr + '</span>';
        html += '    </div>';
        html += '    <div class="uppy-Dashboard-Item-progress">';
        html += '      <div class="uppy-Dashboard-Item-progressBar" style="width: ' + file.progress.percentage + '%"></div>';
        html += '    </div>';
        html += '  </div>';
        html += '  <button type="button" class="uppy-Dashboard-Item-action uppy-Dashboard-Item-action--remove" data-file-id="' + file.id + '" title="파일 제거">×</button>';
        html += '</div>';
      });
    }

    this.filesContainer.innerHTML = html;

    // 삭제 버튼 이벤트
    var removeButtons = this.filesContainer.querySelectorAll('.uppy-Dashboard-Item-action--remove');
    var self = this;
    removeButtons.forEach(function(btn) {
      btn.addEventListener('click', function(e) {
        e.preventDefault();
        var fileId = btn.getAttribute('data-file-id');
        self.uppy.removeFile(fileId);
      });
    });

    console.log('Rendered', files.length, 'files in Dashboard');
  };

  /**
   * 파일 진행률 업데이트
   */
  window.Uppy.Dashboard.prototype.updateFileProgress = function(file, progress) {
    if (!this.filesContainer) return;

    var fileItem = this.filesContainer.querySelector('[data-file-id="' + file.id + '"]');
    if (fileItem) {
      var progressBar = fileItem.querySelector('.uppy-Dashboard-Item-progressBar');
      if (progressBar) {
        var percent = Math.round((progress.bytesUploaded / progress.bytesTotal) * 100);
        progressBar.style.width = percent + '%';
      }
    }
  };

  /**
   * XHRUpload 플러그인
   */
  window.Uppy.XHRUpload = function(uppy, opts) {
    this.uppy = uppy;
    this.type = 'uploader';
    this.id = 'XHRUpload';
    this.opts = Object.assign({
      endpoint: '/upload',
      method: 'POST',
      formData: true,
      fieldName: 'files',
      headers: {},
      timeout: 30000
    }, opts || {});
  };

  /**
   * XHRUpload 설치
   */
  window.Uppy.XHRUpload.prototype.install = function() {
    // 설치 로직
  };

  /**
   * 파일 업로드
   */
  window.Uppy.XHRUpload.prototype.uploadFiles = function(files) {
    var self = this;
    var promises = files.map(function(file) {
      return self.uploadFile(file);
    });

    return Promise.all(promises).then(function(results) {
      var successful = results.filter(function(r) { return r.success; });
      var failed = results.filter(function(r) { return !r.success; });
      
      self.uppy.emit('complete', {
        successful: successful,
        failed: failed
      });

      return { successful: successful, failed: failed };
    });
  };

  /**
   * 단일 파일 업로드
   */
  window.Uppy.XHRUpload.prototype.uploadFile = function(file) {
    var self = this;
    
    return new Promise(function(resolve, reject) {
      var xhr = new XMLHttpRequest();
      var formData = new FormData();

      if (self.opts.formData) {
        formData.append(self.opts.fieldName, file.data, file.name);
      }

      // 진행률 이벤트
      xhr.upload.addEventListener('progress', function(e) {
        if (e.lengthComputable) {
          var bytesUploaded = e.loaded;
          var bytesTotal = e.total;
          var percentage = Math.round((bytesUploaded / bytesTotal) * 100);

          // 파일 진행률 업데이트
          file.progress.bytesUploaded = bytesUploaded;
          file.progress.bytesTotal = bytesTotal;
          file.progress.percentage = percentage;
          file.progress.uploadStarted = file.progress.uploadStarted || Date.now();

          console.log('Upload progress:', file.name, percentage + '%');

          // Uppy 진행률 이벤트 발생
          self.uppy.emit('upload-progress', file, {
            bytesUploaded: bytesUploaded,
            bytesTotal: bytesTotal
          });

          // Dashboard 진행률 업데이트
          self.uppy.setState({
            files: self.uppy.state.files
          });
        }
      });

      // 완료 이벤트
      xhr.addEventListener('load', function() {
        if (xhr.status >= 200 && xhr.status < 300) {
          file.progress.uploadComplete = true;
          file.progress.percentage = 100;
          
          var response;
          try {
            response = JSON.parse(xhr.responseText);
          } catch (e) {
            response = { path: xhr.responseText };
          }

          self.uppy.emit('upload-success', file, response);
          resolve({ success: true, file: file, response: response });
        } else {
          var error = new Error('Upload failed with status: ' + xhr.status);
          self.uppy.emit('upload-error', file, error);
          resolve({ success: false, file: file, error: error });
        }
      });

      // 오류 이벤트
      xhr.addEventListener('error', function() {
        var error = new Error('Upload failed');
        self.uppy.emit('upload-error', file, error);
        resolve({ success: false, file: file, error: error });
      });

      // 요청 전송
      xhr.open(self.opts.method, self.opts.endpoint);
      
      // 헤더 설정
      Object.keys(self.opts.headers).forEach(function(key) {
        xhr.setRequestHeader(key, self.opts.headers[key]);
      });

      xhr.send(formData);
    });
  };

  console.log('Uppy browser bundle loaded successfully');

})(window, document);

